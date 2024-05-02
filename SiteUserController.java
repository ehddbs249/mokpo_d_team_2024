package ce.mnu.siteuser;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpSession;
import org.springframework.data.domain.*;

@Controller
@RequestMapping(path="/siteuser")
public class SiteUserController {
	@Autowired
	private SiteUserRepository userRepository;
	
	@GetMapping(value={"", "/"})
	public String start(Model model) {
		return "start";
	}
	
	@GetMapping(path="/signup")
	public String signup(Model model) {
		model.addAttribute("siteuser", new SiteUser());
		return "signup_input";
	}
	
	@PostMapping(path="/signup")
	public String signup(@ModelAttribute SiteUser user, Model model) {
		userRepository.save(user);
		model.addAttribute("name", user.getName( ));
		return "signup_done";
	}
	
	@PostMapping(path="/find")
	public String findUser(@RequestParam(name="email") String email,
			HttpSession session, Model model,
			RedirectAttributes rd) {
		SiteUser user = userRepository.findByEmail(email);
		if(user != null) {
			model.addAttribute("user", user);
			return "find_done";
		}	
		rd.addFlashAttribute("reason", "wrong email");
		return "redirect:/error";
	}
	
	@GetMapping(path="/find")
	public String find() {
		return "find_user";
	}
	
	@PostMapping(path="/login")
	public String loginUser(@RequestParam(name="email") String email,
			@RequestParam(name="passwd") String passwd,
			HttpSession session,
			RedirectAttributes rd) {
		SiteUser user = userRepository.findByEmail(email);
		if(user != null) {
			if(passwd.equals(user.getPasswd())) {
				session.setAttribute("email", email);
				return "login_done";
			}
		}
		rd.addFlashAttribute("reason", "wrong password");
		return "redirect:/error";
	}
	
	@GetMapping(path="/login")
	public String loginForm() {
		return "login";
	}
	
	@GetMapping(path="/logout")
	public String logout(HttpSession session) {
		session.invalidate();
		return "";
	}
	
	@Autowired
	private ArticleRepository articleRepository;
	
	@GetMapping(path="/bbs/write")
	public String bbsForm(Model model) {
		model.addAttribute("article", new Article());
		return "new_article";
	}
	
	@PostMapping(path="/bbs/add")
	public String addArticle(@ModelAttribute Article article, Model model) {
		articleRepository.save(article);
		model.addAttribute("article", article);
		return "saved";
	}
	
	@GetMapping(path="/bbs")
	public String getAllArticles(@RequestParam(name="pno", defaultValue="0")
		String pno, Model model, HttpSession session,
		RedirectAttributes rd) {
		String email = (String) session.getAttribute("email");
		if(email == null) {
			rd.addFlashAttribute("reason", "login required");
			return "redirect:/error";
		}
		Integer pageNo = 0;
		if(pno != null) {
			pageNo = Integer.valueOf(pno);
		}
		Integer pageSize = 2;
		Pageable paging = PageRequest.of(pageNo, pageSize,
			Sort.Direction.DESC, "num");
		Page<ArticleHeader> data =
				articleRepository.findArticleHeaders(paging);
		model.addAttribute("articles", data);
		return "articles";
	}
	
	@GetMapping(path="/read")
	public String readArticle(@RequestParam(name="num") String num,
			HttpSession session, Model model) {
		Long no = Long.valueOf(num);
		Article article = articleRepository.getReferenceById(no);
		model.addAttribute("article", article);
		return "article";
	}
}