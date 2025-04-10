package dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class Output {
    private List<List<String>> matrix;
    private BigDecimal reward;
    private Map<String, List<String>> appliedWinningCombinations;
    private String appliedBonusSymbol;
}
