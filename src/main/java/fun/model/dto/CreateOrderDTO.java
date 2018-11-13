package fun.model.dto;

import javax.validation.constraints.NotNull;
import java.util.Map;

public class CreateOrderDTO {
    @NotNull
    private String orderByUuid;
    @NotNull
    private Map<String, Integer> parts;

    public String getOrderByUuid() {
        return orderByUuid;
    }

    public void setOrderByUuid(String orderByUuid) {
        this.orderByUuid = orderByUuid;
    }

    public Map<String, Integer> getParts() {
        return parts;
    }

    public void setParts(Map<String, Integer> partNames) {
        this.parts = partNames;
    }
}
