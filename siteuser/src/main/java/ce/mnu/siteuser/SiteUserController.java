package ce.mnu.siteuser;

import java.io.*;
import java.util.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import org.springframework.http.*;
import org.springframework.core.io.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpSession;
import org.springframework.data.domain.*;

@Controller
@RequestMapping(path="/siteuser")
public class SiteUserController {
	@Autowired
	private SiteUserRepository userRepository;
	@Autowired
	private BookRepository bookRepository;
	
	@GetMapping(value = {"","/"})
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
		model.addAttribute("name",user.getName());
		return "signup_done";
	}

	@PostMapping(path="/find")
	public String findUser(@RequestParam(name="email") String email,
		HttpSession session, Model model, RedirectAttributes rd){
			SiteUser user = userRepository.findByEmail(email);
			if(user != null){
				model.addAttribute("user", user);
				return "find_done";
			}
			rd.addFlashAttribute("reason","wrong email");
			return "redirect:/error";
		}

	@GetMapping(path="/find")
	public String find(){
		return "find_user";
	}

	@PostMapping(path = "/shop")
	public String loginUser(@RequestParam(name = "email")String email, @RequestParam(name = "passwd") String passwd, HttpSession session,Model model, RedirectAttributes rd) 
		{
			SiteUser user = userRepository.findByEmail(email);
			if (user != null) 
			{
				if (passwd.equals(user.getPasswd())) 
				{
					session.setAttribute("email", email);
					model.addAttribute("books", bookRepository.findAll());
					return "shop";
				}
				
			}
			rd.addFlashAttribute("reason","wrong password");
			return "redirect:/login";

		}


	@GetMapping(path = "/login")
	public String loginForm()
	{
		return "login_input";
	}

	@GetMapping(path = "/logout")
	public String logout(HttpSession session){
		session.invalidate();
		return "redirect:/siteuser/login";
	}

	// 게시판 관련
	@Autowired
	private ArticleRepository articleRepository;

	@GetMapping(path="/bbs/write") // 글 쓰기 페이지로 이동
	public String bbsForm(Model model){
		model.addAttribute("article", new Article());
		return "new_article";
	}

	@PostMapping(path="/bbs/add")	// 글을 DB에 저장
	public String addArticle(@ModelAttribute Article article, Model model){
		articleRepository.save(article);
		model.addAttribute("article", article);
		return "saved";
	}

	@GetMapping(path="/bbs")	// 전체 글 목록 보기
	public String getAllArticles(@RequestParam(name="pno",defaultValue = "0") String pno, Model model, HttpSession session, RedirectAttributes rd){
		String email = (String) session.getAttribute("email"); 	// 로그인 확인
		if(email == null){
			rd.addFlashAttribute("reason","login required");
			return "redirect:/error"; 	// 로그인 안 했으면, 에러 페이지로
		}
		Integer pageNo = 0;		// 페이지 번호
		if(pno != null){
			pageNo = Integer.valueOf(pno);
		}
		Integer pageSize = 2; 	// 페이지 크기
		Pageable paging = PageRequest.of(pageNo, pageSize, Sort.Direction.DESC, "num");
		Page<ArticleHeader> data = articleRepository.findArticleHeaders(paging);	// 모든 글 읽기
		model.addAttribute("articles", data);
		return "articles";
	}

	@GetMapping(path="/read")
	public String readArticle(@RequestParam(name="num") String num, HttpSession session, Model model){
		Long no = Long.valueOf(num);
		Article article = articleRepository.getReferenceById(no);
		model.addAttribute("article", article);
		return "article";
	}

	// 파일 업로드
	@PostMapping(path="/upload")
	public String upload(@RequestParam MultipartFile file, Model model) throws IllegalStateException, IOException {
		if(!file.isEmpty()){
			String newName = file.getOriginalFilename();
			newName = newName.replace(' ','_');
			FileDto dto = new FileDto(newName, file.getContentType());
			File upfile = new File(dto.getFileName());
			file.transferTo(upfile);
			model.addAttribute("file", dto);
		}
		return "result";
	}
	@GetMapping(path="/upload")
	public String visitUpload(){
		return "uploadForm";
	}

	@Value("${spring.servlet.multipart.location}")
	String base;	// 파일 저장 폴더

	// 파일 다운로드
	@GetMapping(path = "/download")
	public ResponseEntity<Resource> download(@ModelAttribute FileDto dto) throws IOException{
		Path path = Paths.get(base + "/" + dto.getFileName());
		String contentType = Files.probeContentType(path);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentDisposition(ContentDisposition.builder("attachment").filename(dto.getFileName(),StandardCharsets.UTF_8).build());
		headers.add(HttpHeaders.CONTENT_TYPE, contentType);
		Resource rsc = new InputStreamResource(Files.newInputStream(path));
		return new ResponseEntity<>(rsc,headers,HttpStatus.OK);
	}

	@GetMapping("/shop")
    public String showBooks(Model model) {
        model.addAttribute("books", bookRepository.findAll());
        return "shop";
    }
	

	/*
	 * @GetMapping("/shop") public String showBooks(Model model, HttpSession
	 * session) { if (session.getAttribute("email") == null) { return
	 * "redirect:/siteuser/login";} List<Book> books = bookRepository.findAll();
	 * model.addAttribute("books", books); return "shop"; }
	 */


	 @GetMapping("/shop_info/{id}")
    public String showBookDetails(@PathVariable("id") Long id, Model model) {
        Book book = bookRepository.findById(id).orElse(null);
        if (book == null) {
            return ""; // 책 정보가 없는 경우 에러 페이지로 리다이렉트
        }
        model.addAttribute("book", book);
        return "shop_info"; // 책 상세 정보 페이지
    }

	// 마이페이지 조회
	 @GetMapping("/mypage")
	 public String myPage(HttpSession session, Model model) {
		 String email = (String) session.getAttribute("email");
		 if (email == null) {
			 return "redirect:/siteuser/login";
		 }
		 SiteUser user = userRepository.findByEmail(email);
		 if(user==null) {
			 return "redirect:/error";
		 }
		 model.addAttribute("user", user);
		 return "mypage";
	 }

// 정보 수정 페이지
@GetMapping("/mypage/edit")
public String editUser(HttpSession session, Model model) {
    String email = (String) session.getAttribute("email");
    if (email == null) {
        return "redirect:/siteuser/login";
    }
    SiteUser user = userRepository.findByEmail(email);
    model.addAttribute("user", user);
    return "edit_user";
}

// 정보 업데이트
@PostMapping("/mypage/update")
public String updateUser(@ModelAttribute SiteUser updatedUser, HttpSession session, RedirectAttributes rd) {
    String sessionEmail = (String) session.getAttribute("email");
    if (sessionEmail == null) {
        return "redirect:/siteuser/login";
    }
    
    SiteUser existingUser = userRepository.findByEmail(sessionEmail);
    if (existingUser == null) {
        rd.addFlashAttribute("error", "No user found with the email.");
        return "redirect:/siteuser/mypage/edit";
    }

    // 이메일은 업데이트하지 않고, 다른 정보만 업데이트
    existingUser.setName(updatedUser.getName());
    // 필요한 경우 다른 필드도 여기에 추가
    userRepository.save(existingUser);

    return "redirect:/siteuser/mypage";
}

	


}
