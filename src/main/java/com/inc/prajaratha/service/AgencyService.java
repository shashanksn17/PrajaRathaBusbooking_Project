package com.inc.prajaratha.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.inc.prajaratha.dao.AgencyDao;
import com.inc.prajaratha.dao.CustomerDao;
import com.inc.prajaratha.dto.Agency;
import com.inc.prajaratha.dto.Bus;
import com.inc.prajaratha.dto.Route;
import com.inc.prajaratha.dto.Station;
import com.inc.prajaratha.helper.AES;
import com.inc.prajaratha.helper.MailSendingHelper;
import com.inc.prajaratha.repository.BusRepository;
import com.inc.prajaratha.repository.RouteRepository;

import jakarta.servlet.http.HttpSession;

@Service
public class AgencyService {

	@Autowired
	CustomerDao customerDao;

	@Autowired
	MailSendingHelper mailSendingHelper;

	@Autowired
	AgencyDao agencyDao;
	
	@Autowired
	BusRepository busRepository;

	@Autowired
	RouteRepository routeRepository;

	public String signup(Agency agency, BindingResult result, HttpSession session) {
		if (!agency.getPassword().equals(agency.getCpassword()))
			result.rejectValue("cpassword", "error.cpassword", "* Password and Confirm Password Should be Matching");
		if (agencyDao.checkEmail(agency.getEmail()) || customerDao.checkEmail(agency.getEmail()))
			result.rejectValue("email", "error.email", "* Email Should be Unique");
		if (agencyDao.checkMobile(agency.getMobile()) || customerDao.checkMobile(agency.getMobile()))
			result.rejectValue("mobile", "error.mobile", "* Mobile Number Should be Unique");

		if (result.hasErrors())
			return "agencysignup.html";
		else {
			agencyDao.deleteIfExists(agency);
			agency.setCpassword(AES.encrypt(agency.getCpassword(), "123"));
			agency.setPassword(AES.encrypt(agency.getPassword(), "123"));
			agency.setOtp(new Random().nextInt(100000, 1000000));
			System.out.println("Otp - " + agency.getOtp());
			if (mailSendingHelper.sendEmail(agency)) {
				agencyDao.save(agency);
				session.setAttribute("successMessage", "Otp Sent Success");
				return "redirect:/agency/send-otp/" + agency.getId() + "";
			} else {
				session.setAttribute("failMessage", "Sorry Not able to send OTP");
				return "redirect:/agency/signup";
			}

		}
	}

	public String verifyOtp(int id, int otp, HttpSession session) {
		Agency agency = agencyDao.findById(id);
		if (agency.getOtp() == otp) {
			agency.setStatus(true);
			agencyDao.save(agency);
			session.setAttribute("successMessage", "Otp Verified Success, You can Login Now");
			return "redirect:/login";
		} else {
			session.setAttribute("failMessage", "Invalid Otp, Try Again");
			return "redirect:/agency/send-otp/" + agency.getId() + "";
		}
	}

	public String resendOtp(int id, HttpSession session) {
		Agency agency = agencyDao.findById(id);
		agency.setOtp(new Random().nextInt(100000, 1000000));
		System.out.println("Otp - " + agency.getOtp());
		if (mailSendingHelper.sendEmail(agency)) {
			agencyDao.save(agency);
			session.setAttribute("successMessage", "Otp Re-Sent Success");
			return "redirect:/agency/send-otp/" + agency.getId() + "";
		} else {
			session.setAttribute("failMessage", "Failed to Send Otp");
			return "redirect:/agency/send-otp/" + agency.getId() + "";
			
		}
	}

	public String addBus(Bus bus, MultipartFile image, HttpSession session) {
		Agency agency = (Agency) session.getAttribute("agency");
		if (agency == null) {
			session.setAttribute("failMessage", "Invalid Session");
			return "redirect:/";
		} else {
			bus.setImageLink(addToCloudinary(image));
			agency.getBuses().add(bus);
			agencyDao.save(agency);
			session.setAttribute("agency", agencyDao.findById(agency.getId()));
			session.setAttribute("successMessage", "Bus Added Success");
			return "redirect:/";

		}
	}

	public String addToCloudinary(MultipartFile image) {
		Cloudinary cloudinary = new Cloudinary(ObjectUtils.asMap("cloud_name", "dqm61ad4e", "api_key",
				"516934743719299", "api_secret", "41H6nbeRaoWdr0Xtv7fs91TzvjE", "secure", true));

		Map resume = null;
		try {
			Map<String, Object> uploadOptions = new HashMap<String, Object>();
			uploadOptions.put("folder", "Bus");
			resume = cloudinary.uploader().upload(image.getBytes(), uploadOptions);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return (String) resume.get("url");
	}
	
	public String addRoute(HttpSession session, ModelMap map) {
		Agency agency = (Agency) session.getAttribute("agency");
		if (agency == null) {
			session.setAttribute("failMessage", "Invalid Session");
			return "redirect:/";
		} else {
			List<Bus> buses = agency.getBuses();
			if (buses.isEmpty()) {
				session.setAttribute("failMessage", "First Add a Bus");
				return "redirect:/";
			} else {
				map.put("buses", buses);
				return "add-route.html";
			}
		}
	}
	
	public String addRoute(Route route, HttpSession session) {
		Agency agency = (Agency) session.getAttribute("agency");
		if (agency == null) {
			session.setAttribute("failMessage", "Invalid Session");
			return "redirect:/";
		} else {
			Bus bus = busRepository.findById(route.getBus().getId()).orElse(null);
			for (Station station : route.getStations()) {
				station.setRoute(route);
			}
			route.setBus(bus);
			route.setAgency(agency);
			routeRepository.save(route);
			bus.getRoutes().add(route);
			busRepository.save(bus);
			session.setAttribute("agency", agencyDao.findById(agency.getId()));
			session.setAttribute("successMessage", "Route Added Success");
			return "redirect:/";
		}
	}

	public String fetchRoutes(HttpSession session, ModelMap map) {
		Agency agency = (Agency) session.getAttribute("agency");
		if (agency == null) {
			session.setAttribute("failMessage", "Invalid Session");
			return "redirect:/";
		} else {
			List<Bus> buses = agency.getBuses();
			if (buses.isEmpty()) {
				session.setAttribute("failMessage", "No Routes Added Yet");
				return "redirect:/";
			} else {
				List<Integer> list = buses.stream().mapToInt(x -> x.getId()).boxed().collect(Collectors.toList());
				List<Route> routes = routeRepository.findByBus_idIn(list);
				if (routes.isEmpty()) {
					session.setAttribute("failMessage", "No Routes Added Yet");
					return "redirect:/";
				} else {
					map.put("routes", routes);
					return "fetch-routes.html";
				}
			}
		}

	}
	public String deleteRoute(int id, HttpSession session) {
		Agency agency = (Agency) session.getAttribute("agency");
		if (agency == null) {
			session.setAttribute("failMessage", "Invalid Session");
			return "redirect:/";
		} else {
			Bus bus = routeRepository.findById(id).orElseThrow().getBus();
			Route route = null;
			for (Route route1 : bus.getRoutes()) {
				if (route1.getId() == id) {
					route = route1;
					break;
				}
			}
			bus.getRoutes().remove(route);
			busRepository.save(bus);
			routeRepository.delete(route);
			session.setAttribute("agency", agencyDao.findById(agency.getId()));
			session.setAttribute("successMessage", "Route Removed Success");
			return "redirect:/agency/manage-route";
		}
	}
	
	public String editRoute(int id, HttpSession session, ModelMap map) {
		Agency agency = (Agency) session.getAttribute("agency");
		if (agency == null) {
			session.setAttribute("failMessage", "Invalid Session");
			return "redirect:/";
		} else {
			Route route = routeRepository.findById(id).orElseThrow();
			map.put("route", route);
			return "edit-route.html";
		}
	}
	
	public String fetchBuses(HttpSession session, ModelMap map) {
		Agency agency = (Agency) session.getAttribute("agency");
		if (agency == null) {
			session.setAttribute("failMessage", "Invalid Session");
			return "redirect:/";
		} else {
			List<Bus> buses = agency.getBuses();
			if (buses.isEmpty()) {
				session.setAttribute("failMessage", "No Buses Added Yet");
				return "redirect:/";
			} else {
				map.put("buses", buses);
				return "fetch-buses.html";
			}
		}
	}
	public String deleteBus(int id, HttpSession session) {
		Agency agency = (Agency) session.getAttribute("agency");
		if (agency == null) {
			session.setAttribute("failMessage", "Invalid Session");
			return "redirect:/";
		} else {
			Bus bus = null;
			for (Bus bus1 : agency.getBuses()) {
				if (bus1.getId() == id) {
					bus = bus1;
					break;
				}
			}
			agency.getBuses().remove(bus);
			agencyDao.save(agency);
			busRepository.delete(bus);
			session.setAttribute("agency", agencyDao.findById(agency.getId()));
			session.setAttribute("successMessage", "Bus Removed Success");
			return "redirect:/agency/manage-bus";
		}
}
	
	public String editBus(int id, HttpSession session, ModelMap map) {
		Agency agency = (Agency) session.getAttribute("agency");
		if (agency == null) {
			session.setAttribute("failMessage", "Invalid Session");
			return "redirect:/";
		} else {
			Bus bus = busRepository.findById(id).orElseThrow();
			map.put("bus", bus);
			return "edit-bus.html";
		}
	}

	public String editBus(Bus bus, MultipartFile image, HttpSession session) {
		Agency agency = (Agency) session.getAttribute("agency");
		if (agency == null) {
			session.setAttribute("failMessage", "Invalid Session");
			return "redirect:/";
		} else {
			try {
				if (image.getInputStream().available() != 0) {
					bus.setImageLink(addToCloudinary(image));
				} else {
					bus.setImageLink(busRepository.findById(bus.getId()).orElseThrow().getImageLink());
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			busRepository.save(bus);
			session.setAttribute("agency", agencyDao.findById(agency.getId()));
			session.setAttribute("successMessage", "Bus Updated Success");
			return "redirect:/agency/manage-bus";

		}
	}
}