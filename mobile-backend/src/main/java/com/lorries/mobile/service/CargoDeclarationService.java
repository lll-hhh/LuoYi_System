package com.lorries.mobile.service;

import com.lorries.mobile.common.result.PageResult;
import com.lorries.mobile.entity.CargoDeclaration;

/**
 * 货物申报服务接口
 */
public interface CargoDeclarationService {

    PageResult<CargoDeclaration> getMyDeclarations(Integer page, Integer size, String status);

    CargoDeclaration getById(Integer declarationId);

    CargoDeclaration submit(CargoDeclaration declaration);

    void cancel(Integer declarationId);

    void updateStatus(Integer declarationId, String status);
}
