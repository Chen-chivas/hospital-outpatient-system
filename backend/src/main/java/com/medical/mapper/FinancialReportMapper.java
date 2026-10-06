package com.medical.mapper;

import com.medical.entity.FinancialReport;
import org.apache.ibatis.annotations.*;
import java.time.LocalDate;
import java.util.List;

@Mapper
public interface FinancialReportMapper {

    @Select("SELECT * FROM financial_report WHERE report_date = #{date}")
    FinancialReport selectByDate(LocalDate date);

    @Select("SELECT * FROM financial_report WHERE report_date BETWEEN #{startDate} AND #{endDate} ORDER BY report_date DESC")
    List<FinancialReport> selectBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Insert("INSERT INTO financial_report (report_date, total_income, cash_amount, wechat_amount, alipay_amount, insurance_amount) " +
            "VALUES (#{reportDate}, #{totalIncome}, #{cashAmount}, #{wechatAmount}, #{alipayAmount}, #{insuranceAmount})")
    int insert(FinancialReport report);

    @Update("UPDATE financial_report SET total_income=#{totalIncome}, cash_amount=#{cashAmount}, " +
            "wechat_amount=#{wechatAmount}, alipay_amount=#{alipayAmount}, insurance_amount=#{insuranceAmount} WHERE id=#{id}")
    int update(FinancialReport report);
}