package com.example.hotpotrestaurantbooking_backend.enums;

import com.example.hotpotrestaurantbooking_backend.entity.ChiTietGiamGiaMon;
import com.example.hotpotrestaurantbooking_backend.repository.ChiTietGiamGiaMonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AutoUpdateTrangThaiCTGGM {
        private final ChiTietGiamGiaMonRepository repo;

      @Scheduled(cron = "0 0 0 * * ?")
    public void capNhatTrangThai(){
            LocalDate homNay=LocalDate.now();
            List<ChiTietGiamGiaMon> ds= repo.findAllByTrangThai(0);

            ds.forEach(ct ->{
                if (ct.getDotGiamGia() !=null
                && ct.getDotGiamGia().getNgayKetThuc() !=null
                && ct.getDotGiamGia().getNgayKetThuc().isBefore(homNay)
                ){
                    ct.setTrangThai(1);
                }
            });
            repo.saveAll(ds);
            System.out.println("Đã cập nhật trạng thái hết hạn");
        }

}
