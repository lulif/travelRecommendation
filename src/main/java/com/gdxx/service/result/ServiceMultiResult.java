package com.gdxx.service.result;

import java.util.List;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/*
 * service层数据封装
 */
@Setter
@Getter
public class ServiceMultiResult<T> {
    private boolean success;
    private int total;
    private List<T> result;

    public ServiceMultiResult(boolean success) {
        this.success = success;
    }

    public ServiceMultiResult(boolean success, int total, List<T> result) {
        this.success = success;
        this.total = total;
        this.result = result;
    }

    public static <T> ServiceMultiResult<T> success() {
        return new ServiceMultiResult<>(true);
    }

    public static <T> ServiceMultiResult<T> of(List<T> result) {
        ServiceMultiResult<T> ServiceMultiResult = new ServiceMultiResult<>(true);
        ServiceMultiResult.setResult(result);
        return ServiceMultiResult;
    }

}
