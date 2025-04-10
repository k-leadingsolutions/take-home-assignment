package dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Symbol {
    @JsonProperty("reward_multiplier")
    private BigDecimal rewardMultiplier;
    private String type;
    private BigDecimal extra;
    private String impact;
}
