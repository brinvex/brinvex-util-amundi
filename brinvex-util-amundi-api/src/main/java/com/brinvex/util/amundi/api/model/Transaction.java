/*
 * Copyright © 2023 Brinvex (dev@brinvex.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.brinvex.util.amundi.api.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.StringJoiner;

public class Transaction implements Serializable {

    private String id;

    private String accountNumber;

    private TransactionType type;

    private LocalDate orderDay;

    private BigDecimal netAmount;

    private BigDecimal fees;

    private BigDecimal quantity;

    private BigDecimal price;

    private LocalDate priceDay;

    private Currency currency;

    private String isin;

    private String instrumentName;

    private String description;

    private LocalDate settleDay;

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public LocalDate getOrderDay() {
        return orderDay;
    }

    public void setOrderDay(LocalDate orderDay) {
        this.orderDay = orderDay;
    }

    public LocalDate getSettleDay() {
        return settleDay;
    }

    public void setSettleDay(LocalDate settleDay) {
        this.settleDay = settleDay;
    }

    public String getIsin() {
        return isin;
    }

    public void setIsin(String isin) {
        this.isin = isin;
    }

    public String getInstrumentName() {
        return instrumentName;
    }

    public void setInstrumentName(String instrumentName) {
        this.instrumentName = instrumentName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getNetAmount() {
        return netAmount;
    }

    public void setNetAmount(BigDecimal netAmount) {
        this.netAmount = netAmount;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public BigDecimal getFees() {
        return fees;
    }

    public void setFees(BigDecimal fees) {
        this.fees = fees;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public LocalDate getPriceDay() {
        return priceDay;
    }

    public void setPriceDay(LocalDate priceDay) {
        this.priceDay = priceDay;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Transaction.class.getSimpleName() + "[", "]")
                .add("accountNumber='" + accountNumber + "'")
                .add("type=" + type)
                .add("orderDay=" + orderDay)
                .add("grossAmount=" + netAmount)
                .add("fees=" + fees)
                .add("quantity=" + quantity)
                .add("price=" + price)
                .add("priceDay=" + priceDay)
                .add("currency=" + currency)
                .add("isin='" + isin + "'")
                .add("instrumentName='" + instrumentName + "'")
                .add("description='" + description + "'")
                .add("settleDay=" + settleDay)
                .toString();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
