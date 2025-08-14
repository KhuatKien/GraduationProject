package com.phenikaa.tourService.mapper;

import com.phenikaa.tourService.dto.request.AddTourImageRequest;
import com.phenikaa.tourService.dto.request.UpdateTourImageRequest;
import com.phenikaa.tourService.entity.TourImage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UpdateTourImageMapper {
    TourImage toEntity(AddTourImageRequest dto);

    TourImage toEntity(UpdateTourImageRequest dto);

    List<TourImage> toEntityList(List<AddTourImageRequest> dtoList);

    void updateEntity(UpdateTourImageRequest dto, @MappingTarget TourImage entity);

    /**
     * Xử lý cập nhật, xóa và thêm mới images
     */
    default void updateImageCollection(List<UpdateTourImageRequest> dtoList, List<TourImage> existingImages) {
        if (dtoList == null) {
            existingImages.clear();
            return;
        }

        // Tạo map các images có ID để dễ tra cứu
        Map<Integer, UpdateTourImageRequest> dtoMap = dtoList.stream()
                .filter(dto -> dto.getImageId() != null)
                .collect(Collectors.toMap(UpdateTourImageRequest::getImageId, dto -> dto));

        // Xóa các images không còn trong danh sách update
        existingImages.removeIf(existing ->
                existing.getImageId() != null && !dtoMap.containsKey(existing.getImageId())
        );

        // Cập nhật các images có ID
        for (TourImage existingImage : existingImages) {
            if (existingImage.getImageId() != null && dtoMap.containsKey(existingImage.getImageId())) {
                UpdateTourImageRequest dto = dtoMap.get(existingImage.getImageId());
                updateEntity(dto, existingImage);
            }
        }

        // Thêm mới các images không có ID
        List<TourImage> newImages = dtoList.stream()
                .filter(dto -> dto.getImageId() == null)
                .map(this::toEntity)
                .collect(Collectors.toList());

        existingImages.addAll(newImages);
    }
}
