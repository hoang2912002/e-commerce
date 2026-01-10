package com.fashion.product.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fashion.product.common.annotation.ApiMessageResponse;
import com.fashion.product.dto.request.OptionValueRequest;
import com.fashion.product.dto.request.search.SearchRequest;
import com.fashion.product.dto.response.OptionValueResponse;
import com.fashion.product.dto.response.PaginationResponse;
import com.fashion.product.mapper.OptionValueMapper;
import com.fashion.product.service.OptionValueService;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/optionValues")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OptionValueController {
    OptionValueService optionValueService;
    OptionValueMapper optionValueMapper;

    @PostMapping("")
    @ApiMessageResponse("option.value.success.create")
    public ResponseEntity<OptionValueResponse> createOptionValue(
        @RequestBody @Validated(OptionValueRequest.Create.class) OptionValueRequest optionValue
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.optionValueService.createOptionValue(optionValueMapper.toValidated(optionValue)));
    }
    
    @PutMapping("")
    @ApiMessageResponse("option.value.success.update")
    public ResponseEntity<OptionValueResponse> updateOptionValue(
        @RequestBody @Validated(OptionValueRequest.Update.class) OptionValueRequest optionValue
    ) {        
        return ResponseEntity.ok(this.optionValueService.updateOptionValue(optionValueMapper.toValidated(optionValue)));
    }

    @GetMapping("/{id}")
    @ApiMessageResponse("option.value.success.get.single")
    public ResponseEntity<OptionValueResponse> getOptionValueById(
        @PathVariable("id") Long id
    ) {
        return ResponseEntity.ok(this.optionValueService.getOptionValueById(id));
    }
    
    @GetMapping("")
    @ApiMessageResponse("option.value.success.get.all")
    public ResponseEntity<PaginationResponse<List<OptionValueResponse>>> getAllOptionValue(
        @ModelAttribute SearchRequest request
    ) {
        return ResponseEntity.ok(this.optionValueService.getAllOptionValue(request));
    }

    @DeleteMapping("/{id}")
    @ApiMessageResponse("option.value.success.delete")
    public ResponseEntity<Void> deleteOptionValueById(
        @PathVariable("id") Long id
    ){
        return ResponseEntity.ok(null);

    }
}
