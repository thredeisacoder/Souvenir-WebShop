package project.demo.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/blog")
public class BlogController {

    @GetMapping
    public String blogPage(Model model) {
        // In a real application, these would be fetched from a database
        List<Map<String, Object>> blogPosts = new ArrayList<>();
        
        // Blog post 1
        Map<String, Object> post1 = new HashMap<>();
        post1.put("id", 1);
        post1.put("title", "Quà lưu niệm truyền thống Việt Nam: Nét đẹp văn hóa dân tộc");
        post1.put("summary", "Khám phá những món quà lưu niệm truyền thống đặc sắc của Việt Nam mang đậm bản sắc văn hóa dân tộc");
        post1.put("content", "Việt Nam với chiều dài lịch sử và nền văn hóa phong phú đã sản sinh ra nhiều loại hình thủ công mỹ nghệ độc đáo. Từ những chiếc nón lá, áo dài, tranh đông hồ đến các sản phẩm gốm sứ Bát Tràng, những món quà lưu niệm này không chỉ mang giá trị sử dụng mà còn chứa đựng cả tâm hồn của người Việt. Mỗi món quà đều kể một câu chuyện về lịch sử, văn hóa và con người Việt Nam...");
        post1.put("image", "/static/assets/blog/traditional-souvenirs.jpg");
        post1.put("author", "Nguyễn Văn A");
        post1.put("date", "23/04/2025");
        post1.put("tags", List.of("truyền thống", "văn hóa", "thủ công"));
        
        // Blog post 2
        Map<String, Object> post2 = new HashMap<>();
        post2.put("id", 2);
        post2.put("title", "Top 10 món quà lưu niệm độc đáo từ các vùng miền Việt Nam");
        post2.put("summary", "Gợi ý những món quà đặc trưng từ các vùng miền khác nhau trên khắp Việt Nam");
        post2.put("content", "Mỗi vùng miền của Việt Nam lại có những đặc sản riêng, mang đặc trưng của địa phương đó. Từ Bắc vào Nam, du khách có thể tìm thấy vô vàn những món quà lưu niệm ý nghĩa. Miền Bắc nổi tiếng với gốm sứ Bát Tràng, tranh dân gian Đông Hồ. Miền Trung có các sản phẩm từ làng nghề Hội An, nón lá Huế. Miền Nam lại hấp dẫn với các sản phẩm thủ công từ dừa Bến Tre, các món đồ lưu niệm từ chợ nổi Cái Răng...");
        post2.put("image", "/static/assets/blog/regional-souvenirs.jpg");
        post2.put("author", "Trần Thị B");
        post2.put("date", "15/04/2025");
        post2.put("tags", List.of("đặc sản", "vùng miền", "du lịch"));
        
        // Blog post 3
        Map<String, Object> post3 = new HashMap<>();
        post3.put("id", 3);
        post3.put("title", "Xu hướng quà lưu niệm hiện đại - Kết hợp truyền thống và đương đại");
        post3.put("summary", "Những món quà lưu niệm hiện đại mang hơi thở của thời đại nhưng vẫn giữ được tinh hoa văn hóa dân tộc");
        post3.put("content", "Trong thời đại hiện nay, các nhà thiết kế trẻ đang tạo ra một làn sóng mới trong ngành thủ công mỹ nghệ Việt Nam. Họ khéo léo kết hợp những giá trị truyền thống với thiết kế hiện đại, tạo nên những sản phẩm lưu niệm vừa mang đậm bản sắc dân tộc vừa phù hợp với thị hiếu người tiêu dùng hiện đại. Từ những chiếc túi xách được thêu họa tiết dân gian đến những món đồ trang trí nội thất với chất liệu truyền thống như mây tre đan, gốm sứ...");
        post3.put("image", "/static/assets/blog/modern-souvenirs.jpg");
        post3.put("author", "Lê Văn C");
        post3.put("date", "08/04/2025");
        post3.put("tags", List.of("hiện đại", "sáng tạo", "kết hợp"));
        
        // Blog post 4
        Map<String, Object> post4 = new HashMap<>();
        post4.put("id", 4);
        post4.put("title", "Quà lưu niệm handmade - Giá trị từ bàn tay và trái tim");
        post4.put("summary", "Khám phá giá trị độc đáo và ý nghĩa từ những món quà được làm thủ công");
        post4.put("content", "Trong thời đại công nghiệp hóa, những sản phẩm handmade lại càng trở nên quý giá hơn bao giờ hết. Mỗi món quà thủ công đều mang dấu ấn riêng của người nghệ nhân, là sự kết hợp giữa kỹ thuật, sự sáng tạo và tâm huyết. Từ những chiếc khăn len đan tay, những món đồ trang sức bằng hạt cườm đến các sản phẩm giấy dó thủ công, mỗi sản phẩm đều chứa đựng câu chuyện và tình cảm khiến chúng trở nên đặc biệt...");
        post4.put("image", "/static/assets/blog/handmade-gifts.jpg");
        post4.put("author", "Phạm Thị D");
        post4.put("date", "01/04/2025");
        post4.put("tags", List.of("handmade", "thủ công", "quà tặng"));
        
        blogPosts.add(post1);
        blogPosts.add(post2);
        blogPosts.add(post3);
        blogPosts.add(post4);
        
        model.addAttribute("blogPosts", blogPosts);
        return "blog/blog-list";
    }
    
    @GetMapping("/{id}")
    public String blogDetail(@PathVariable("id") Integer id, Model model) {
        // In a real application, this would be fetched from a database
        Map<String, Object> post = new HashMap<>();
        
        // Sample data based on ID
        if (id == 1) {
            post.put("id", 1);
            post.put("title", "Quà lưu niệm truyền thống Việt Nam: Nét đẹp văn hóa dân tộc");
            post.put("content", "Việt Nam với chiều dài lịch sử và nền văn hóa phong phú đã sản sinh ra nhiều loại hình thủ công mỹ nghệ độc đáo. Từ những chiếc nón lá, áo dài, tranh đông hồ đến các sản phẩm gốm sứ Bát Tràng, những món quà lưu niệm này không chỉ mang giá trị sử dụng mà còn chứa đựng cả tâm hồn của người Việt.\n\nMỗi món quà đều kể một câu chuyện về lịch sử, văn hóa và con người Việt Nam. Nón lá - biểu tượng văn hóa lâu đời của người Việt, không chỉ là vật dụng che nắng che mưa mà còn là hình ảnh gắn liền với người phụ nữ Việt Nam, là nguồn cảm hứng bất tận cho thơ ca, âm nhạc.\n\nTranh dân gian Đông Hồ với những nét vẽ mộc mạc, giản dị nhưng chứa đựng triết lý sâu sắc về cuộc sống và ước mơ của người nông dân Việt Nam. Mỗi bức tranh là một câu chuyện cổ tích, một lời chúc phúc, một mong ước về cuộc sống ấm no, hạnh phúc.\n\nGốm sứ Bát Tràng với lịch sử phát triển hơn 700 năm, mang trong mình dấu ấn của nhiều thời kỳ lịch sử. Từ những chiếc bát đĩa thường ngày đến những tác phẩm nghệ thuật tinh xảo, gốm Bát Tràng luôn chinh phục người dùng bởi vẻ đẹp mộc mạc và chất lượng bền bỉ.\n\nQuà lưu niệm truyền thống Việt Nam không chỉ là món quà mang về sau chuyến du lịch, mà còn là cầu nối văn hóa, giúp người nước ngoài hiểu hơn về đất nước và con người Việt Nam. Đối với người Việt, đó còn là niềm tự hào về bản sắc văn hóa dân tộc, là di sản quý giá cần được gìn giữ và phát huy trong thời đại hiện nay.");
            post.put("image", "/static/assets/blog/traditional-souvenirs-detail.jpg");
            post.put("author", "Nguyễn Văn A");
            post.put("date", "23/04/2025");
            post.put("tags", List.of("truyền thống", "văn hóa", "thủ công"));
        } else if (id == 2) {
            post.put("id", 2);
            post.put("title", "Top 10 món quà lưu niệm độc đáo từ các vùng miền Việt Nam");
            post.put("content", "Mỗi vùng miền của Việt Nam lại có những đặc sản riêng, mang đặc trưng của địa phương đó. Từ Bắc vào Nam, du khách có thể tìm thấy vô vàn những món quà lưu niệm ý nghĩa.\n\n1. Gốm sứ Bát Tràng (Hà Nội): Với lịch sử hàng trăm năm, làng gốm Bát Tràng nổi tiếng với các sản phẩm gốm sứ chất lượng cao và hoa văn tinh tế. Từ bộ ấm trà, bát đĩa đến các tượng trang trí, mỗi sản phẩm đều thể hiện bàn tay khéo léo của người thợ gốm Việt Nam.\n\n2. Tranh dân gian Đông Hồ (Bắc Ninh): Những bức tranh với họa tiết truyền thống mang đậm tính nhân văn và ước vọng của người dân Việt Nam. Tranh Đông Hồ là biểu tượng văn hóa quý giá và là món quà lưu niệm ý nghĩa.\n\n3. Nón lá Huế: Nón lá Huế nổi tiếng với vành nón mỏng, nhẹ và hoa văn được vẽ bên trong nón. Dưới ánh sáng mặt trời, những hoa văn này sẽ hiện lên như một bức tranh nghệ thuật.\n\n4. Lụa Hội An: Lụa Hội An mềm mại, óng ả với nhiều màu sắc tươi sáng. Từ khăn choàng, áo dài đến các phụ kiện nhỏ, lụa Hội An luôn là món quà được yêu thích.\n\n5. Cà phê Buôn Ma Thuột: Vùng Tây Nguyên nổi tiếng với cà phê chất lượng cao. Du khách có thể mua cà phê đóng gói hoặc các sản phẩm từ cà phê như socola, bánh kẹo.\n\n6. Mây tre đan Phú Vinh (Hà Tây cũ): Từ giỏ xách, hộp đựng đồ đến các món đồ trang trí nội thất, sản phẩm mây tre đan thủ công tinh tế và bền đẹp.\n\n7. Đồ thủ công từ dừa Bến Tre: Từ vỏ, cùi, đến lá dừa đều được tận dụng để tạo ra các sản phẩm thủ công độc đáo như túi xách, mũ nón, hộp đựng...\n\n8. Tò he (đồ chơi làm từ bột gạo nếp): Món đồ chơi truyền thống với nhiều hình dáng, màu sắc rực rỡ, là ký ức tuổi thơ của nhiều thế hệ người Việt.\n\n9. Tranh cát Phan Thiết: Những bức tranh được tạo thành từ cát nhiều màu sắc, thể hiện phong cảnh, con người Việt Nam đầy nghệ thuật.\n\n10. Đá quý Yên Bái: Vùng đất Yên Bái nổi tiếng với các loại đá quý như ngọc bích, ruby, sapphire. Những món trang sức từ đá quý là món quà giá trị và độc đáo.");
            post.put("image", "/static/assets/blog/regional-souvenirs-detail.jpg");
            post.put("author", "Trần Thị B");
            post.put("date", "15/04/2025");
            post.put("tags", List.of("đặc sản", "vùng miền", "du lịch"));
        } else if (id == 3) {
            post.put("id", 3);
            post.put("title", "Xu hướng quà lưu niệm hiện đại - Kết hợp truyền thống và đương đại");
            post.put("content", "Trong thời đại hiện nay, các nhà thiết kế trẻ đang tạo ra một làn sóng mới trong ngành thủ công mỹ nghệ Việt Nam. Họ khéo léo kết hợp những giá trị truyền thống với thiết kế hiện đại, tạo nên những sản phẩm lưu niệm vừa mang đậm bản sắc dân tộc vừa phù hợp với thị hiếu người tiêu dùng hiện đại.\n\nXu hướng tối giản (Minimalism) trong thiết kế quà lưu niệm đang được ưa chuộng. Các nhà thiết kế lấy cảm hứng từ các họa tiết truyền thống như hoa sen, trống đồng... nhưng thể hiện theo phong cách đơn giản, tinh tế trên các sản phẩm như áo phông, túi vải, ly sứ.\n\nSự kết hợp giữa chất liệu truyền thống và công nghệ hiện đại cũng tạo nên những sản phẩm độc đáo. Ví dụ, các sản phẩm từ mây tre đan kết hợp với kim loại, gốm sứ kết hợp với đèn LED, tạo nên những món đồ trang trí nội thất vừa mang tính nghệ thuật vừa có công năng thực tế.\n\nNhững món đồ lưu niệm có tính tương tác cũng đang trở thành xu hướng. Các sản phẩm như sổ tay có thể tùy chỉnh, đồ chơi dân gian được thiết kế lại để phù hợp với lối sống hiện đại, hay các ứng dụng di động kết hợp với sản phẩm vật lý để tạo trải nghiệm tương tác thú vị.\n\nMột xu hướng quan trọng khác là phát triển bền vững. Các sản phẩm lưu niệm thân thiện với môi trường, sử dụng nguyên liệu tái chế hoặc tự nhiên, được sản xuất theo quy trình bền vững đang ngày càng được ưa chuộng. Đây không chỉ là xu hướng nhất thời mà còn là định hướng phát triển lâu dài của ngành thủ công mỹ nghệ Việt Nam.\n\nSự phát triển của thương mại điện tử cũng mở ra cơ hội mới cho các sản phẩm lưu niệm Việt Nam tiếp cận với thị trường quốc tế. Nhiều thương hiệu quà lưu niệm Việt đã thành công xây dựng hình ảnh chuyên nghiệp và thu hút khách hàng từ khắp nơi trên thế giới.\n\nCó thể nói, xu hướng quà lưu niệm hiện đại của Việt Nam đang phát triển theo hướng kết hợp hài hòa giữa truyền thống và đương đại, giữa giá trị văn hóa và công năng thực tế, giữa thẩm mỹ và tính bền vững. Đây là hướng đi đúng đắn, giúp ngành thủ công mỹ nghệ Việt Nam không chỉ bảo tồn được các giá trị truyền thống mà còn phát triển bền vững trong bối cảnh toàn cầu hóa.");
            post.put("image", "/static/assets/blog/modern-souvenirs-detail.jpg");
            post.put("author", "Lê Văn C");
            post.put("date", "08/04/2025");
            post.put("tags", List.of("hiện đại", "sáng tạo", "kết hợp"));
        } else if (id == 4) {
            post.put("id", 4);
            post.put("title", "Quà lưu niệm handmade - Giá trị từ bàn tay và trái tim");
            post.put("content", "Trong thời đại công nghiệp hóa, những sản phẩm handmade lại càng trở nên quý giá hơn bao giờ hết. Mỗi món quà thủ công đều mang dấu ấn riêng của người nghệ nhân, là sự kết hợp giữa kỹ thuật, sự sáng tạo và tâm huyết.\n\nGiá trị của quà lưu niệm handmade không chỉ nằm ở vẻ đẹp bên ngoài mà còn ở câu chuyện đằng sau mỗi sản phẩm. Mỗi món quà là kết quả của quá trình lao động sáng tạo, là sự tích lũy kinh nghiệm và kỹ năng của người thợ thủ công. Khi tặng hoặc sở hữu một món quà handmade, chúng ta đang gìn giữ và lan tỏa những giá trị văn hóa, những kỹ năng truyền thống.\n\nTính độc đáo là một trong những đặc điểm nổi bật của quà lưu niệm handmade. Không có hai món quà thủ công nào hoàn toàn giống nhau, dù được tạo ra bởi cùng một người. Mỗi sản phẩm đều có những nét riêng, những \"khiếm khuyết\" nhỏ tạo nên vẻ đẹp tự nhiên và chân thực.\n\nQuà lưu niệm handmade còn mang ý nghĩa môi trường khi phần lớn đều được tạo ra từ các nguyên liệu tự nhiên, thân thiện với môi trường. Nhiều nghệ nhân còn sử dụng nguyên liệu tái chế, biến những vật dụng bỏ đi thành những tác phẩm nghệ thuật độc đáo.\n\nĐối với người nhận, món quà handmade còn mang giá trị tình cảm đặc biệt. Thời gian, công sức và tâm huyết người tạo ra hoặc người lựa chọn món quà đã gửi gắm vào đó là điều không thể đo đếm bằng tiền bạc. Chính vì vậy, những món quà thủ công thường để lại ấn tượng sâu sắc và được trân trọng lâu dài.\n\nTrong xu hướng quay trở lại với các giá trị truyền thống và bền vững, quà lưu niệm handmade đang ngày càng được ưa chuộng. Nhiều người trẻ đã bắt đầu tìm hiểu và thực hành các kỹ thuật thủ công truyền thống, không chỉ để tạo ra những món quà đẹp mà còn để kết nối với văn hóa và lịch sử.\n\nCó thể nói, quà lưu niệm handmade là sự kết tinh của kỹ thuật, nghệ thuật và tình cảm. Trong một thế giới ngày càng số hóa và tự động hóa, những món quà được tạo ra từ bàn tay khéo léo và trái tim nhiệt thành của con người lại càng trở nên đáng quý. Đó không đơn thuần là một món đồ vật mà là một phần của văn hóa, một câu chuyện và một kỷ niệm đáng trân trọng.");
            post.put("image", "/static/assets/blog/handmade-gifts-detail.jpg");
            post.put("author", "Phạm Thị D");
            post.put("date", "01/04/2025");
            post.put("tags", List.of("handmade", "thủ công", "quà tặng"));
        } else {
            // Default post if ID not found
            post.put("id", 0);
            post.put("title", "Bài viết không tồn tại");
            post.put("content", "Bài viết bạn đang tìm kiếm không có trong hệ thống.");
            post.put("image", "/static/assets/blog/default.jpg");
            post.put("author", "Admin");
            post.put("date", "29/04/2025");
            post.put("tags", List.of());
        }
        
        // Recent posts (would come from database in real application)
        List<Map<String, Object>> recentPosts = new ArrayList<>();
        
        Map<String, Object> recent1 = new HashMap<>();
        recent1.put("id", 1);
        recent1.put("title", "Quà lưu niệm truyền thống Việt Nam: Nét đẹp văn hóa dân tộc");
        recent1.put("date", "23/04/2025");
        
        Map<String, Object> recent2 = new HashMap<>();
        recent2.put("id", 2);
        recent2.put("title", "Top 10 món quà lưu niệm độc đáo từ các vùng miền Việt Nam");
        recent2.put("date", "15/04/2025");
        
        Map<String, Object> recent3 = new HashMap<>();
        recent3.put("id", 3);
        recent3.put("title", "Xu hướng quà lưu niệm hiện đại - Kết hợp truyền thống và đương đại");
        recent3.put("date", "08/04/2025");
        
        recentPosts.add(recent1);
        recentPosts.add(recent2);
        recentPosts.add(recent3);
        
        model.addAttribute("post", post);
        model.addAttribute("recentPosts", recentPosts);
        return "blog/blog-detail";
    }
}