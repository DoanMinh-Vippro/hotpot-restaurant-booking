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

    @Override
    public DTOBanResponse add(DTOBanRequest request) {
        Ban b = new Ban();
        b.setLoaiBan(request.getLoaiBan());
        b.setTrangThai(request.getTrangThai());

        KhuVuc k = khuVucRepository.findById(request.getIdKhuVuc()).orElseThrow(() -> new CustomResourceNotFoundException("khong tim thay khu vuc"));

        b.setKhuVuc(k);
        banRepository.save(b);
        b.setTenBan("B"+b.getIdBan());
        banRepository.save(b);
        DTOBanResponse response = mapper.map(b,DTOBanResponse.class);
        response.setTenKhuVuc(
                b.getKhuVuc().getTenKhuVuc()
        );
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
