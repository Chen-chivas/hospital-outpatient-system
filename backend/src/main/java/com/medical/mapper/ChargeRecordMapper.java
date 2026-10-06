package com.medical.mapper;

import com.medical.entity.ChargeRecord;
import org.apache.ibatis.annotations.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface ChargeRecordMapper {

    @Select("SELECT * FROM charge_record WHERE patient_id = #{patientId} AND status = '待支付'")
    List<ChargeRecord> selectPendingByPatientId(Long patientId);

    @Select("SELECT * FROM charge_record WHERE id = #{id}")
    ChargeRecord selectById(Long id);

    @Select("SELECT * FROM charge_record WHERE charge_no = #{chargeNo}")
    ChargeRecord selectByChargeNo(String chargeNo);

    @Select("SELECT * FROM charge_record WHERE prescription_id = #{prescriptionId}")
    ChargeRecord selectByPrescriptionId(Long prescriptionId);

    @Select("SELECT * FROM charge_record WHERE status = '已支付' AND charge_time BETWEEN #{startTime} AND #{endTime}")
    List<ChargeRecord> selectPaidBetween(@Param("startTime") LocalDateTime startTime,
                                         @Param("endTime") LocalDateTime endTime);

    @Select("SELECT * FROM charge_record WHERE status = '已支付' AND DATE(charge_time) = CURDATE()")
    List<ChargeRecord> selectTodayPaid();

    @Select("SELECT SUM(total_amount) FROM charge_record WHERE status = '已支付' AND DATE(charge_time) = CURDATE()")
    BigDecimal selectTodayIncome();

    @Select("SELECT payment_method, SUM(total_amount) as amount FROM charge_record " +
            "WHERE status = '已支付' AND charge_time BETWEEN #{startTime} AND #{endTime} " +
            "GROUP BY payment_method")
    List<Map<String, Object>> selectPaymentMethodStats(@Param("startTime") LocalDateTime startTime,
                                                       @Param("endTime") LocalDateTime endTime);

    @Select("SELECT DATE(charge_time) as date, SUM(total_amount) as amount " +
            "FROM charge_record WHERE status = '已支付' AND charge_time BETWEEN #{startTime} AND #{endTime} " +
            "GROUP BY DATE(charge_time) ORDER BY date")
    List<Map<String, Object>> selectDailyIncome(@Param("startTime") LocalDateTime startTime,
                                                @Param("endTime") LocalDateTime endTime);

    @Insert("INSERT INTO charge_record (patient_id, patient_name, charge_no, total_amount, status, prescription_id, charge_time) " +
            "VALUES (#{patientId}, #{patientName}, #{chargeNo}, #{totalAmount}, #{status}, #{prescriptionId}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ChargeRecord record);

    @Update("UPDATE charge_record SET status=#{status}, payment_method=#{paymentMethod}, " +
            "insurance_amount=#{insuranceAmount}, self_pay=#{selfPay}, charge_time=NOW(), invoice_no=#{invoiceNo} WHERE id=#{id}")
    int update(ChargeRecord record);

    @Update("UPDATE charge_record SET status='已退费', remark=#{remark} WHERE id=#{id}")
    int refund(@Param("id") Long id, @Param("remark") String remark);
}
