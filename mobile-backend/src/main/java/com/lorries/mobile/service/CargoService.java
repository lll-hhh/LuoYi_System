package com.lorries.mobile.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lorries.mobile.common.result.PageResult;
import com.lorries.mobile.dto.CargoVO;
import com.lorries.mobile.entity.Cargo;

/**
 * 货物服务接口
 */
public interface CargoService extends IService<Cargo> {

    /**
     * 根据追踪号查询货物
     */
    CargoVO getByTrackingNo(String trackingNo);

    /**
     * 获取货物详情
     */
    CargoVO getCargoDetail(Long cargoId);

    /**
     * 获取货物列表
     */
    PageResult<CargoVO> getCargoList(String status, String cargoType, String keyword,
                                     Integer page, Integer pageSize);

    /**
     * 更新货物状态
     */
    void updateStatus(Long cargoId, String status, String currentLocation);

    /**
     * 货物签收
     */
    void signCargo(Long cargoId, String signedBy, String signatureImage);

    /**
     * 扫码入库
     */
    void scanInbound(String trackingNo, Long warehouseId, String location);

    /**
     * 扫码出库
     */
    void scanOutbound(String trackingNo, Long taskId);
}
