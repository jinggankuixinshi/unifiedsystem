package com.unified.sales.service;

import com.baomidou.dynamic.datasource.annotation.DS;

import com.unified.common.util.SequenceGenerator;
import com.unified.sales.entity.SalContract;
import com.unified.sales.entity.SalSalesOrder;
import com.unified.sales.entity.SalSalesOrderItem;
import com.unified.sales.mapper.SalContractMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@DS("sales")
@RequiredArgsConstructor
public class ContractService extends com.baomidou.mybatisplus.extension.service.impl.ServiceImpl<SalContractMapper, SalContract> {

    /**
     * 根据销售报单自动生成合同
     */
    public SalContract generateContract(SalSalesOrder order, List<SalSalesOrderItem> items) {
        SalContract contract = new SalContract();
        contract.setContractNo(SequenceGenerator.generate("CT"));
        contract.setSalesOrderId(order.getId());
        contract.setSignStatus(0);

        String contentJson = buildContractContent(order, items);
        contract.setContentJson(contentJson);

        save(contract);
        return contract;
    }

    private String buildContractContent(SalSalesOrder order, List<SalSalesOrderItem> items) {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy年MM月dd日"));
        String itemsJson = items.stream()
                .map(i -> String.format(
                        "{\"productId\":%d,\"spec\":\"%s\",\"qty\":%s,\"unitPrice\":%s,\"amount\":%s}",
                        i.getProductId(), i.getSpecification(), i.getQuantity(), i.getUnitPrice(), i.getAmount()))
                .collect(Collectors.joining(","));

        return String.format(
                "{\"contractNo\":\"%s\",\"date\":\"%s\",\"orderNo\":\"%s\",\"totalAmount\":%s,\"paymentMethod\":\"%s\",\"deliveryDate\":\"%s\",\"warranty\":\"%s\",\"items\":[%s]}",
                SequenceGenerator.generate("CT"), today, order.getOrderNo(),
                order.getTotalAmount(), order.getPaymentMethod(),
                order.getDeliveryDate(), order.getWarrantyTerms(), itemsJson);
    }
}
