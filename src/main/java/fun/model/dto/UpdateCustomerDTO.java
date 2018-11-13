package fun.model.dto;

import javax.validation.constraints.NotNull;

public class UpdateCustomerDTO {
    @NotNull
    private String uuid;
    @NotNull
    private String emailAddress;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }
}
