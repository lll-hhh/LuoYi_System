package com.lorries.mobile.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lorries.mobile.entity.CargoDeclaration;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 货物申报Mapper
 */
@Mapper
public interface CargoDeclarationMapper extends BaseMapper<CargoDeclaration> {

    List<CargoDeclaration> findByUserId(@Param("userId") Integer userId);
}
