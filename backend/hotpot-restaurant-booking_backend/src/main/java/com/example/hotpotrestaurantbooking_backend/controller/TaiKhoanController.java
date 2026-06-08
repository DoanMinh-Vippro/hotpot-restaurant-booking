    package  com.example.hotpotrestaurantbooking_backend.controller;


    import com.example.hotpotrestaurantbooking_backend.dto.DTOTaiKhoanRequest;
    import com.example.hotpotrestaurantbooking_backend.dto.DTOTaiKhoanResponse;
    import com.example.hotpotrestaurantbooking_backend.service.TaiKhoanService;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.web.bind.annotation.*;

    import java.util.List;

    @RestController
    @RequestMapping("api/tai-khoan")
    @CrossOrigin(origins = "*")
    public class TaiKhoanController {
        @Autowired
        private TaiKhoanService service;

        @GetMapping("/hien-thi")
        public List<DTOTaiKhoanResponse> getAll() {
            return service.getAll();
        }

        @GetMapping("/detail/{id}")
        public DTOTaiKhoanResponse findById(@PathVariable Integer id) {
            return service.findById(id);
        }

        @PostMapping("/add")
        public DTOTaiKhoanResponse add(@RequestBody DTOTaiKhoanRequest request) {
            return service.add(request);
        }

        @PutMapping("/update/{id}")
        public DTOTaiKhoanResponse update(@PathVariable Integer id,
                                          @RequestBody DTOTaiKhoanRequest request) {
            return service.update(id, request);
        }

        @DeleteMapping("/delete/{id}")
        public void delete(@PathVariable Integer id) {
            service.delete(id);
        }
    }
