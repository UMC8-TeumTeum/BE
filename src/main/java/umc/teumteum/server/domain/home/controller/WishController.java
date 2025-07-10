package umc.teumteum.server.domain.home.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import umc.teumteum.server.global.apiPayload.ApiResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/wishes")
@Tag(name = "wish", description = "위시 관련 API")
public class WishController {

    @GetMapping(value = "/wishlist", produces = "application/json")
    @Operation(summary = "위시리스트 정보 조회 API",description = "위시 목록을 조회하는 API입니다. query string으로 조회 기간과 page 번호를 입력주세요.")
    public ApiResponse<String> getWishlist(@RequestParam("duration") String duration, @RequestParam("page") Long page){
        return ApiResponse.onSuccess(null);
    }

    @PostMapping(value = "", consumes = "application/json", produces = "application/json")
    @Operation(summary = "위시 등록 API",description = "새로운 위시를 생성하는 API입니다.")
    public ApiResponse<String> createWish(){
        return ApiResponse.onSuccess(null);
    }

    @GetMapping(value = "/{wishId}", produces = "application/json")
    @Operation(summary = "특정 위시 정보 조회 API",description = "특정위시의 상세정보를 조회하는 API입니다.")
    public ApiResponse<String> getWish(@PathVariable("wishId") Long wishId){
        return ApiResponse.onSuccess(null);
    }

    @PatchMapping(value = "/{wishId}", consumes = "application/json", produces = "application/json")
    @Operation(summary = "특정 위시 정보 수정 API",description = "특정위시의 상세정보를 수정하는 API입니다.")
    public ApiResponse<String> updateWish(@PathVariable("wishId") Long wishId){
        return ApiResponse.onSuccess(null);
    }

    @GetMapping(value = "/categories", produces = "application/json")
    @Operation(summary = "카테고리 정보 조회 API",description = "위시의 카테고리 정보를 조회하는 API입니다.")
    public ApiResponse<String> getCategory(){
        return ApiResponse.onSuccess(null);
    }

    @DeleteMapping(value = "", consumes = "application/json")
    @Operation(summary = "위시 삭제 API",description = "위시를 삭제하는 API입니다.")
    public ApiResponse<String> deleteWish(){
        return ApiResponse.onSuccess(null);
    }

    @GetMapping(value = "/teum", produces = "application/json")
    @Operation(summary = "위시 등록 가능 시간 조회 API",description = "위시리스트를 틈으로 등록가능한 시간을 조회하는 API입니다. query string으로 am 또는 pm을 입력헤주세요")
    public ApiResponse<String> getEnableTimes(@RequestParam("period") String period){
        return ApiResponse.onSuccess(null);
    }

    @PostMapping(value = "/{wishId}/assign", consumes = "application/json", produces = "application/json")
    @Operation(summary = "위시 투두 등록 API",description = "위시를 투두로 등록하는 API입니다.")
    public ApiResponse<String> assignWish(@PathVariable("wishId") Long wishId){
        return ApiResponse.onSuccess(null);
    }

}
