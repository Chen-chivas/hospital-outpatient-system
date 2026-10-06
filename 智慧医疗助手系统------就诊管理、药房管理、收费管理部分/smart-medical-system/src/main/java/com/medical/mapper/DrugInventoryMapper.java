package com.medical.mapper;

import com.medical.entity.DrugInventory;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface DrugInventoryMapper {

    @Select("SELECT * FROM drug_inventory")
    List<DrugInventory> selectAll();

    @Select("SELECT * FROM drug_inventory WHERE id = #{id}")
    DrugInventory selectById(Long id);

    @Select("SELECT * FROM drug_inventory WHERE drug_name = #{name}")
    DrugInventory selectByName(String name);

    @Select("SELECT * FROM drug_inventory WHERE drug_code = #{drugCode}")
    DrugInventory selectByCode(String drugCode);

    @Select("SELECT * FROM drug_inventory WHERE drug_name LIKE CONCAT('%', #{name}, '%')")
    List<DrugInventory> searchByName(String name);

    @Select("SELECT * FROM drug_inventory WHERE current_stock < min_stock")
    List<DrugInventory> selectWarningStock();

    @Insert("INSERT INTO drug_inventory (drug_code, drug_name, specification, manufacturer, current_stock, min_stock, " +
            "retail_price, location) VALUES (#{drugCode}, #{drugName}, #{specification}, #{manufacturer}, " +
            "#{currentStock}, #{minStock}, #{retailPrice}, #{location})")
    int insert(DrugInventory drug);

    @Update("UPDATE drug_inventory SET current_stock = current_stock - #{quantity} WHERE drug_code = #{drugCode}")
    int deductStock(@Param("drugCode") String drugCode, @Param("quantity") Integer quantity);

    @Update("UPDATE drug_inventory SET current_stock = #{currentStock}, min_stock = #{minStock}, " +
            "retail_price = #{retailPrice}, location = #{location} WHERE id = #{id}")
    int update(DrugInventory drug);
}