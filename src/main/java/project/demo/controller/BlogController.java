package project.demo.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller for the blog feature with in-memory blog posts
 */
@Controller
@RequestMapping("/blog")
public class BlogController {

    private static final List<BlogPost> blogPosts = initBlogPosts();

    /**
     * Display the list of blog posts
     */
    @GetMapping
    public String listBlogPosts(Model model) {
        model.addAttribute("blogPosts", blogPosts);
        return "blog/list";
    }

    /**
     * Display a single blog post by its slug
     */
    @GetMapping("/{slug}")
    public String viewBlogPost(@PathVariable("slug") String slug, Model model) {
        // Find the blog post by slug
        BlogPost post = blogPosts.stream()
                .filter(p -> p.getSlug().equals(slug))
                .findFirst()
                .orElse(null);
        
        if (post == null) {
            return "redirect:/blog";
        }
        
        model.addAttribute("blogPost", post);
        
        // Get recent posts for the sidebar (limit to 5)
        List<BlogPost> recentPosts = new ArrayList<>(blogPosts);
        if (recentPosts.size() > 5) {
            recentPosts = recentPosts.subList(0, 5);
        }
        model.addAttribute("recentPosts", recentPosts);
        
        return "blog/detail";
    }

    /**
     * Search for blog posts
     */
    @GetMapping("/search")
    public String searchBlogPosts(@RequestParam("query") String query, Model model) {
        // Simple search based on title or excerpt
        String lowercaseQuery = query.toLowerCase();
        List<BlogPost> searchResults = blogPosts.stream()
                .filter(p -> p.getTitle().toLowerCase().contains(lowercaseQuery) || 
                           (p.getExcerpt() != null && p.getExcerpt().toLowerCase().contains(lowercaseQuery)))
                .toList();
        
        model.addAttribute("blogPosts", searchResults);
        model.addAttribute("searchTerm", query);
        model.addAttribute("resultCount", searchResults.size());
        return "blog/list";
    }
    
    /**
     * Initialize sample blog posts
     */
    private static List<BlogPost> initBlogPosts() {
        List<BlogPost> posts = new ArrayList<>();
        
        posts.add(new BlogPost(
            "Các món quà lưu niệm truyền thống Việt Nam được yêu thích nhất",
            "cac-mon-qua-luu-niem-truyen-thong-viet-nam-duoc-yeu-thich-nhat",
            "<p>Việt Nam tự hào với nền văn hóa phong phú và lâu đời, được thể hiện qua những món quà lưu niệm truyền thống độc đáo. Những món quà này không chỉ đẹp mắt mà còn mang đậm dấu ấn văn hóa Việt, là món quà hoàn hảo để du khách mang về làm kỷ niệm sau chuyến đi.</p>"
            + "<h2>1. Nón lá</h2>"
            + "<p>Nón lá là biểu tượng văn hóa đặc trưng của Việt Nam, được làm từ lá cọ và tre. Không chỉ là vật dụng che nắng, che mưa hữu ích, nón lá còn là món quà lưu niệm được nhiều du khách ưa chuộng. Đặc biệt, nón lá Huế với những hoa văn, tranh vẽ tinh tế bên trong càng làm tăng giá trị nghệ thuật của món đồ này.</p>"
            + "<h2>2. Tranh Đông Hồ</h2>"
            + "<p>Tranh dân gian Đông Hồ là loại hình nghệ thuật dân gian truyền thống của Việt Nam, xuất phát từ làng Đông Hồ, Bắc Ninh. Tranh được in từ các bản khắc gỗ với màu sắc tự nhiên rực rỡ, thường mô tả các chủ đề về cuộc sống nông thôn, động vật và các câu chuyện dân gian. Mỗi bức tranh đều mang ý nghĩa cầu mong may mắn, hạnh phúc và thịnh vượng.</p>"
            + "<h2>3. Gốm sứ Bát Tràng</h2>"
            + "<p>Làng gốm Bát Tràng nổi tiếng với những sản phẩm gốm sứ tinh xảo. Từ bát đĩa, bình hoa đến các món đồ trang trí, mỗi sản phẩm đều được làm thủ công với kỹ thuật men sứ độc đáo và hoa văn trang trí phong phú. Gốm sứ Bát Tràng vừa mang tính thực tiễn cao, vừa là món quà lưu niệm mang đậm nét văn hóa Việt.</p>"
            + "<h2>4. Lụa Việt Nam</h2>"
            + "<p>Lụa Việt Nam nổi tiếng với chất lượng mềm mại, mịn màng và bền đẹp. Các sản phẩm từ lụa như khăn quàng, áo dài, túi xách không chỉ đẹp mắt mà còn thể hiện sự tinh tế và trang nhã. Làng nghề lụa Vạn Phúc (Hà Nội) và làng lụa Mã Châu (Quảng Nam) là những địa chỉ nổi tiếng sản xuất lụa truyền thống chất lượng cao.</p>"
            + "<h2>5. Đồ thủ công mỹ nghệ từ tre, mây</h2>"
            + "<p>Các sản phẩm thủ công mỹ nghệ từ tre, mây như giỏ đựng, đèn trang trí, hộp đựng trang sức... được làm hoàn toàn bằng thủ công với kỹ thuật đan lát tinh xảo. Những sản phẩm này không chỉ mang tính thẩm mỹ cao mà còn thân thiện với môi trường, thể hiện lối sống bền vững của người Việt.</p>"
            + "<p>Khi lựa chọn quà lưu niệm Việt Nam, bạn không chỉ mang về nhà một món đồ đẹp mắt mà còn là một phần văn hóa, lịch sử và tâm hồn Việt. Mỗi món quà đều là sản phẩm của sự sáng tạo, kỹ thuật và tình yêu của người thợ thủ công Việt Nam, giúp lan tỏa nét đẹp văn hóa Việt Nam ra thế giới.</p>",
            "/assets/img/blogs/souvenir1.jpg",
            "Admin",
            LocalDateTime.now().minusDays(5),
            "Việt Nam tự hào với nền văn hóa phong phú và lâu đời, được thể hiện qua những món quà lưu niệm truyền thống độc đáo. Bài viết giới thiệu 5 món quà lưu niệm truyền thống Việt Nam được yêu thích nhất."
        ));
        
        posts.add(new BlogPost(
            "Nghệ thuật làm tranh giấy xoắn - Quilling Art",
            "nghe-thuat-lam-tranh-giay-xoan-quilling-art",
            "<p>Nghệ thuật làm tranh giấy xoắn (Quilling Art) là một hình thức nghệ thuật cổ xưa, nhưng đang ngày càng phổ biến trong thế giới quà tặng và đồ lưu niệm hiện đại. Kỹ thuật này tạo ra những tác phẩm tinh xảo bằng cách xoắn và uốn các dải giấy màu thành các hình dạng khác nhau.</p>"
            + "<h2>Lịch sử của nghệ thuật Quilling</h2>"
            + "<p>Quilling có nguồn gốc từ thời Ai Cập cổ đại và sau đó được phát triển mạnh ở châu Âu vào thế kỷ 15-16, khi các nữ tu sĩ dùng các dải giấy mỏng từ sách tôn giáo để trang trí các vật dụng tôn giáo. Tên gọi \"Quilling\" xuất phát từ việc ban đầu người ta cuộn giấy quanh lông chim (quill).</p>"
            + "<h2>Kỹ thuật cơ bản</h2>"
            + "<p>Để bắt đầu với nghệ thuật Quilling, bạn cần một số nguyên liệu cơ bản:</p>"
            + "<ul>"
            + "<li>Giấy quilling chuyên dụng (dải giấy màu dài, mỏng)</li>"
            + "<li>Dụng cụ cuộn giấy</li>"
            + "<li>Keo dán giấy</li>"
            + "<li>Nhíp và kim</li>"
            + "<li>Thước đo và bảng định hình</li>"
            + "<li>Kéo nhỏ</li>"
            + "</ul>"
            + "<p>Các hình dạng cơ bản trong Quilling bao gồm:</p>"
            + "<ol>"
            + "<li><strong>Vòng tròn cơ bản</strong>: Cuộn dải giấy thành một vòng tròn, sau đó dán cố định đầu cuối.</li>"
            + "<li><strong>Giọt nước</strong>: Từ vòng tròn cơ bản, bóp nhẹ một đầu để tạo hình giọt nước.</li>"
            + "<li><strong>Vuông/Hình chữ nhật</strong>: Bóp cả bốn cạnh của vòng tròn cơ bản để tạo thành hình vuông hoặc chữ nhật.</li>"
            + "<li><strong>Hình ngôi sao</strong>: Bóp nhiều điểm trên vòng tròn để tạo thành hình ngôi sao.</li>"
            + "</ol>"
            + "<h2>Ứng dụng trong đồ lưu niệm</h2>"
            + "<p>Tranh Quilling đã trở thành món quà lưu niệm độc đáo và cá nhân hóa cao. Một số ứng dụng phổ biến bao gồm:</p>"
            + "<ul>"
            + "<li>Thiệp chúc mừng</li>"
            + "<li>Tranh trang trí mini đặt bàn</li>"
            + "<li>Khung ảnh trang trí</li>"
            + "<li>Hộp quà</li>"
            + "<li>Đồ trang sức nhẹ như bông tai, mặt dây chuyền</li>"
            + "</ul>"
            + "<h2>Tại sao Quilling là món quà lưu niệm tuyệt vời?</h2>"
            + "<p>Quilling là món quà lưu niệm hoàn hảo vì nó:</p>"
            + "<ul>"
            + "<li>Độc đáo và cá nhân hóa cao</li>"
            + "<li>Thể hiện sự tỉ mỉ và tinh tế</li>"
            + "<li>Nhẹ và dễ vận chuyển</li>"
            + "<li>Thân thiện với môi trường (sử dụng giấy)</li>"
            + "<li>Có thể kết hợp với nhiều chủ đề và màu sắc khác nhau</li>"
            + "</ul>"
            + "<p>Nghệ thuật Quilling đang ngày càng phổ biến tại Việt Nam, với nhiều người thợ thủ công tài năng tạo ra những tác phẩm tinh xảo. Những món quà lưu niệm từ nghệ thuật này không chỉ đẹp mắt mà còn mang ý nghĩa sâu sắc, thể hiện sự tỉ mỉ và tâm huyết của người tạo ra nó.</p>",
            "/assets/img/blogs/souvenir2.jpg",
            "Nguyễn Văn A",
            LocalDateTime.now().minusDays(10),
            "Khám phá nghệ thuật làm tranh giấy xoắn (Quilling Art) - một kỹ thuật thủ công tinh tế đang ngày càng phổ biến trong lĩnh vực quà lưu niệm. Tìm hiểu lịch sử, kỹ thuật cơ bản và các ứng dụng của Quilling trong đồ lưu niệm."
        ));
        
        posts.add(new BlogPost(
            "Sức hút của đồ thủ công mỹ nghệ hiện đại",
            "suc-hut-cua-do-thu-cong-my-nghe-hien-dai",
            "<p>Trong thời đại số hóa, đồ thủ công mỹ nghệ hiện đại đang tạo nên một làn sóng mới trên thị trường quà lưu niệm. Những sản phẩm này kết hợp giữa kỹ thuật truyền thống và thiết kế đương đại, tạo nên những món quà độc đáo và mang đậm dấu ấn cá nhân.</p>"
            + "<h2>Xu hướng kết hợp truyền thống và hiện đại</h2>"
            + "<p>Các nghệ nhân và nhà thiết kế trẻ đang tìm cách kết hợp kỹ thuật thủ công truyền thống với thẩm mỹ hiện đại. Họ giữ lại kỹ thuật làm thủ công nhưng áp dụng vào những sản phẩm phù hợp với lối sống đương đại như đồ trang trí nội thất, phụ kiện thời trang, hoặc vật dụng hàng ngày.</p>"
            + "<h2>Tính ứng dụng cao</h2>"
            + "<p>Không chỉ đẹp mắt, đồ thủ công mỹ nghệ hiện đại còn chú trọng vào tính ứng dụng. Đây là điểm khác biệt quan trọng so với đồ thủ công truyền thống vốn thường mang tính trưng bày nhiều hơn. Các sản phẩm như túi xách thủ công, đèn trang trí từ mây tre đan, hoặc đồ gốm sứ hiện đại vừa mang giá trị mỹ thuật vừa phục vụ nhu cầu sử dụng hàng ngày.</p>"
            + "<h2>Câu chuyện đằng sau mỗi sản phẩm</h2>"
            + "<p>Điều thu hút người mua đối với đồ thủ công mỹ nghệ không chỉ là vẻ đẹp bên ngoài mà còn là câu chuyện đằng sau mỗi sản phẩm. Nhiều thương hiệu đồ thủ công hiện đại chú trọng vào việc kể câu chuyện về nguồn gốc, quy trình sản xuất, và ý nghĩa văn hóa của sản phẩm, tạo nên giá trị cảm xúc và kết nối sâu sắc với người dùng.</p>"
            + "<h2>Tính bền vững và thân thiện môi trường</h2>"
            + "<p>Trong bối cảnh ý thức về môi trường ngày càng cao, đồ thủ công mỹ nghệ hiện đại thường được làm từ các nguyên liệu tự nhiên, có thể tái chế hoặc phân hủy sinh học. Nhiều thương hiệu còn áp dụng các phương pháp sản xuất bền vững, hỗ trợ cộng đồng địa phương và thúc đẩy thương mại công bằng.</p>"
            + "<h2>Một số xu hướng đồ thủ công mỹ nghệ hiện đại nổi bật</h2>"
            + "<ol>"
            + "<li><strong>Đồ gốm tối giản</strong>: Các sản phẩm gốm sứ với thiết kế đơn giản, màu sắc trung tính nhưng vẫn giữ được nét đặc trưng của kỹ thuật làm gốm thủ công.</li>"
            + "<li><strong>Trang sức thủ công từ nguyên liệu tái chế</strong>: Các món trang sức được chế tác từ kim loại tái chế, gỗ, hoặc thậm chí là nhựa tái chế, mang đến vẻ đẹp độc đáo và thân thiện môi trường.</li>"
            + "<li><strong>Đồ nội thất từ mây tre đan</strong>: Mây tre đan truyền thống được áp dụng vào các thiết kế nội thất hiện đại, tạo nên những món đồ vừa thân thiện môi trường vừa mang đậm phong cách đương đại.</li>"
            + "<li><strong>Vải dệt thủ công với họa tiết hiện đại</strong>: Kỹ thuật dệt thủ công truyền thống kết hợp với họa tiết và màu sắc hiện đại, tạo nên những sản phẩm vải độc đáo dùng trong thời trang và trang trí nội thất.</li>"
            + "</ol>"
            + "<p>Đồ thủ công mỹ nghệ hiện đại đang chứng minh rằng giá trị của nghề thủ công truyền thống vẫn có chỗ đứng trong thế giới hiện đại. Bằng cách kết hợp giữa kỹ thuật lâu đời và thẩm mỹ đương đại, những món đồ này không chỉ là quà lưu niệm mà còn là cầu nối giữa quá khứ và hiện tại, giữa truyền thống và đổi mới.</p>",
            "/assets/img/blogs/souvenir3.jpg",
            "Trần Thị B",
            LocalDateTime.now().minusDays(15),
            "Khám phá sức hút của đồ thủ công mỹ nghệ hiện đại - sự kết hợp hoàn hảo giữa kỹ thuật truyền thống và thẩm mỹ đương đại. Bài viết phân tích các xu hướng thiết kế, tính ứng dụng và giá trị bền vững của các sản phẩm thủ công hiện đại."
        ));
        
        return posts;
    }
    
    /**
     * Blog Post class to store blog article information
     */
    public static class BlogPost {
        private String title;
        private String slug;
        private String content;
        private String imageUrl;
        private String author;
        private LocalDateTime publishedDate;
        private String excerpt;
        
        public BlogPost(String title, String slug, String content, String imageUrl, 
                String author, LocalDateTime publishedDate, String excerpt) {
            this.title = title;
            this.slug = slug;
            this.content = content;
            this.imageUrl = imageUrl;
            this.author = author;
            this.publishedDate = publishedDate;
            this.excerpt = excerpt;
        }

        public String getTitle() {
            return title;
        }

        public String getSlug() {
            return slug;
        }

        public String getContent() {
            return content;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public String getAuthor() {
            return author;
        }

        public LocalDateTime getPublishedDate() {
            return publishedDate;
        }

        public String getExcerpt() {
            return excerpt;
        }
    }
}