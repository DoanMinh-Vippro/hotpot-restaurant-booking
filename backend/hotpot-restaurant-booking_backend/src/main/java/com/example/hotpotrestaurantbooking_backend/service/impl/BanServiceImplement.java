package com.example.hotpotrestaurantbooking_backend.service.impl;

import com.example.hotpotrestaurantbooking_backend.dto.DTOBanRequest;
import com.example.hotpotrestaurantbooking_backend.dto.DTOBanResponse;
import com.example.hotpotrestaurantbooking_backend.entity.Ban;
import com.example.hotpotrestaurantbooking_backend.entity.KhuVuc;
import com.example.hotpotrestaurantbooking_backend.exception.CustomResourceNotFoundException;
import com.example.hotpotrestaurantbooking_backend.repository.BanRepository;
import com.example.hotpotrestaurantbooking_backend.repository.KhuVucRepository;
import com.example.hotpotrestaurantbooking_backend.service.BanService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class BanServiceImplement implements BanService {
    private final BanRepository banRepository;
    private final KhuVucRepository khuVucRepository;
    private final ModelMapper mapper;


    @Override
    public List<DTOBanResponse> getAll() {
        return banRepository
                .findAll()
                .stream()
                .map(b -> {
                    DTOBanResponse response = mapper.map(b,DTOBanResponse.class);
                    response.setTenKhuVuc(
                            b.getKhuVuc().getTenKhuVuc());
                    return response;
                })
                .toList();
    }

    @Override
    public DTOBanResponse findById(Integer id) {
        return banRepository
                .findById(id)
                .map(b -> {
                    DTOBanResponse response = mapper.map(b,DTOBanResponse.class);
                    response.setTenKhuVuc(
                            b.getKhuVuc().getTenKhuVuc());
                    return response;
                })
                .orElseThrow(()-> new CustomResourceNotFoundException("khong tim thay id: " + id));
    }
//========================================================================
private int getNextSoThuTu(KhuVuc khuVuc) { //tạo mã số của tên bàn
    return banRepository.findByKhuVuc_IdKhuVuc(khuVuc.getIdKhuVuc())
            .stream()
            .map(Ban::getTenBan)
            .filter(Objects::nonNull)
            .filter(name -> name.startsWith(khuVuc.getMaKhuVuc()))
            .map(name -> name.substring(khuVuc.getMaKhuVuc().length()))
            .mapToInt(Integer::parseInt)
            .max()
            .orElse(0) + 1;
}

    @Override
    public DTOBanResponse add(DTOBanRequest request) {

        KhuVuc khuVuc = khuVucRepository.findById(request.getIdKhuVuc())
                .orElseThrow(() -> new CustomResourceNotFoundException("Không tìm thấy khu vực"));
        Ban ban = new Ban();
        ban.setLoaiBan(request.getLoaiBan());
        ban.setTrangThai(request.getTrangThai());
        ban.setKhuVuc(khuVuc);

        int stt = getNextSoThuTu(khuVuc);
        ban.setTenBan(khuVuc.getMaKhuVuc() + stt);
        banRepository.save(ban);
        DTOBanResponse response = mapper.map(ban, DTOBanResponse.class);
        response.setTenKhuVuc(khuVuc.getTenKhuVuc());
        return response;
    }

    @Override
    public DTOBanResponse update(Integer id, DTOBanRequest request) {
        return banRepository
                .findById(id)
                .map(b -> {
                    if (request.getLoaiBan() != null) b.setLoaiBan(request.getLoaiBan());
                    if (request.getIdKhuVuc() != null ){
                        KhuVuc k = khuVucRepository
                                .findById(request.getIdKhuVuc())
                                .orElseThrow(() -> new CustomResourceNotFoundException("Khong tim thay khu vuc"));
                        b.setKhuVuc(k);
                    }
                    if (request.getTrangThai() != null ) b.setTrangThai(request.getTrangThai());
                    banRepository.save(b);
                    DTOBanResponse response = mapper.map(b, DTOBanResponse.class);
                    response.setTenKhuVuc(b.getKhuVuc().getTenKhuVuc());//set tay tên khu vực
                    return response;
                })
                .orElseThrow(() -> new CustomResourceNotFoundException("khong tim thay ban nay"));
    }

    @Override
    public void delete(Integer id) {
        banRepository.deleteById(id);
    }
}
