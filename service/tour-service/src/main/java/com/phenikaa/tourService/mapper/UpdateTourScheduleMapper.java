package com.phenikaa.tourService.mapper;

import com.phenikaa.tourService.dto.request.AddTourScheduleRequest;
import com.phenikaa.tourService.dto.request.UpdateTourScheduleRequest;
import com.phenikaa.tourService.entity.TourSchedule;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UpdateTourScheduleMapper {
    TourSchedule toEntity(AddTourScheduleRequest dto);

    TourSchedule toEntity(UpdateTourScheduleRequest dto);

    List<TourSchedule> toEntityList(List<AddTourScheduleRequest> dtoList);

    @Mapping(target = "scheduleId", ignore = true) // Không update ID
    void updateEntity(UpdateTourScheduleRequest dto, @MappingTarget TourSchedule entity);

    /**
     * Xử lý cập nhật, xóa và thêm mới schedules
     */
    default void updateScheduleCollection(List<UpdateTourScheduleRequest> dtoList, List<TourSchedule> existingSchedules) {
        if (dtoList == null) {
            existingSchedules.clear();
            return;
        }

        // Tạo map các schedules có ID để dễ tra cứu
        Map<Integer, UpdateTourScheduleRequest> dtoMap = dtoList.stream()
                .filter(dto -> dto.getScheduleId() != null)
                .collect(Collectors.toMap(UpdateTourScheduleRequest::getScheduleId, dto -> dto));

        // Xóa các schedules không còn trong danh sách update
        existingSchedules.removeIf(existing ->
                existing.getScheduleId() != null && !dtoMap.containsKey(existing.getScheduleId())
        );

        // Cập nhật các schedules có ID
        for (TourSchedule existingSchedule : existingSchedules) {
            if (existingSchedule.getScheduleId() != null && dtoMap.containsKey(existingSchedule.getScheduleId())) {
                UpdateTourScheduleRequest dto = dtoMap.get(existingSchedule.getScheduleId());
                updateEntity(dto, existingSchedule);
            }
        }

        // Thêm mới các schedules không có ID
        List<TourSchedule> newSchedules = dtoList.stream()
                .filter(dto -> dto.getScheduleId() == null)
                .map(this::toEntity)
                .collect(Collectors.toList());

        existingSchedules.addAll(newSchedules);
    }

}
