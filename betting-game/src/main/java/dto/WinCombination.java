package dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class WinCombination {
    @JsonProperty("reward_multiplier")
    private BigDecimal rewardMultiplier;
    private String when;
    private String group;
    private Integer count;
    @JsonProperty("covered_areas")
    private List<List<String>> coveredAreas;
}
