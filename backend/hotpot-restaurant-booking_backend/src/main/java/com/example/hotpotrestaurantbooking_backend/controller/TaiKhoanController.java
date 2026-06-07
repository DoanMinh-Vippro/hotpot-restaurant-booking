    package org.example.datlich.controller;

    import org.example.datlich.dto.DTOTaiKhoanRequest;
    import org.example.datlich.dto.DTOTaiKhoanResponse;
    import org.example.datlich.entity.TaiKhoan;
    import org.example.datlich.service.TaiKhoanService;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.web.bind.annotation.*;

    import java.util.List;

    @RestController
    @RequestMapping("/tai-khoan")
    @CrossOrigin(origins = "*")
    public class TaiKhoanController {
        @Autowired
        private TaiKhoanService service;

        @GetMapping("/hien-thi")
        public List<DTOTaiKhoanResponse> getAll() {
            return service.getAll();
        }

        @GetMapping("/detail/{id}")
        public DTOTaiKhoanResponse getById(@PathVariable Integer id) {
            return service.getById(id);
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
