package com.lorries.mobile.service;

import com.lorries.mobile.common.result.PageResult;
import com.lorries.mobile.entity.CargoDeclaration;

/**
 * 货物申报服务接口
 */
public interface CargoDeclarationService {

    PageResult<CargoDeclaration> getMyDeclarations(Long userId, Integer page, Integer size, String status);

    CargoDeclaration getMyDeclaration(Long userId, Integer declarationId);

    CargoDeclaration submit(Long userId, CargoDeclaration declaration);

    void cancel(Long userId, Integer declarationId);

    void updateStatus(Integer declarationId, String status);
}
