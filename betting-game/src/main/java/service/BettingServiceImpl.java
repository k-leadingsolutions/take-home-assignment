package service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import dto.BettingGame;
import dto.Output;
import dto.WinCombination;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

public class BettingServiceImpl implements BettingService {

    /**
     * Method to process config.json file and create BettingGame object for
     * processing the scratch game
     *
     * @param filename
     * @return BettingGame
     */
    @Override
    public BettingGame processFile(String filename) {

        BettingGame bettingGame;
        String path = Objects.requireNonNull(getClass().getClassLoader().getResource(filename.trim())).getFile();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new Jdk8Module());
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        try {
            bettingGame = objectMapper.readValue(new File(path), BettingGame.class);
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse file " + e.getMessage());
        }

        return bettingGame;
    }

    /**
     * Method to apply logic for the scratch game bet
     *
     * @param bet_amount
     * @param bettingGame
     * @return String value of Output object
     */
    @Override
    public String bet(BigDecimal bet_amount, BettingGame bettingGame) {

        ObjectMapper objectMapper = new ObjectMapper();
        //dto to output the bet result
        Output output = new Output();
        String result;
        //random generator for random matrix values
        Random random = new Random();
        BigDecimal reward = bet_amount;
        List<List<String>> matrix = new ArrayList<>();
        Set<String> bonusSymbols = bettingGame.getProbabilities().getBonusSymbols().getSymbols().keySet();
        List<String> standardSymbols = bettingGame.getProbabilities().getStandardSymbols().stream()
                .flatMap(s -> s.getSymbols().keySet().stream())
                .toList();
        List<WinCombination> winCombinations = new ArrayList<>(bettingGame.getWinCombinations().values());

        try {

            //set the matrix
            output.setMatrix(generateMatrix(bettingGame, bonusSymbols, standardSymbols, random, matrix));

            //get the applied winning combinations
            Map<String, List<String>> appliedWinningCombinations = getAppliedWinningsMap(matrix, winCombinations);

            // get winning combination reward
            reward = getReward(matrix, winCombinations, reward);

            // get the applied bonus symbol
            String appliedBonusSymbol = matrix.stream()
                    .flatMap(List::stream)
                    .filter(bonusSymbols::contains)
                    .sorted()
                    .findFirst()
                    .orElse(null);

            //set the reward if there's a bonus
            output.setReward(getReward(reward, appliedBonusSymbol));
            // set applied winning combination
            output.setAppliedWinningCombinations(appliedWinningCombinations);

            // set the applied bonus symbol
            output.setAppliedBonusSymbol(appliedBonusSymbol);
            result = objectMapper.writeValueAsString(output);

        } catch (IOException e) {
            throw new RuntimeException("Failed to compute result" + e.getMessage());
        }

        return result;
    }

    /**
     * Method to extract applied winnings group
     * @param matrix
     * @param winCombinations
     * @return
     */
    private static Map<String, List<String>> getAppliedWinningsMap(List<List<String>> matrix, List<WinCombination> winCombinations) {
        Map<String, List<String>> appliedWinningCombinations = new HashMap<>();
        int sameSymbolsCount = 0;
        for (List<String> strings : matrix) {
            for (String string : strings) {
                if (string.equals(strings.get(0))) {
                    sameSymbolsCount++;
                }else{
                    sameSymbolsCount=0;
                }
                if(sameSymbolsCount >= 3){
                    int finalSameSymbolsCount = sameSymbolsCount;
                    appliedWinningCombinations.put(string, winCombinations.stream()
                                    .filter(w -> w.getCount() != null)
                                    .filter(w ->  w.getCount().equals(finalSameSymbolsCount ))
                            .map(WinCombination::getGroup)
                            .distinct().toList());
                }
            }
        }
        return appliedWinningCombinations;
    }

    /**
     * Method to calculate rewards with winning combinations
     *
     * @param matrix
     * @param winCombinations
     * @param reward
     * @return
     */
    private static BigDecimal getReward(List<List<String>> matrix, List<WinCombination> winCombinations, BigDecimal reward) {
        int sameSymbolsCount = 0;
        for (List<String> symbols : matrix) {
            for (String symbol : symbols) {
                if (symbol.equals(symbols.get(0))) {
                    sameSymbolsCount++;
                }
                else {
                    sameSymbolsCount = 0;
                }
            }
        }

        int finalSameSymbolsCount = sameSymbolsCount;
        if(finalSameSymbolsCount != 0) {
            int winCombinationCount = winCombinations.stream()
                    .filter(w -> w.getCount() != null)
                    .filter(w -> w.getCount() == finalSameSymbolsCount)
                    .map(WinCombination::getRewardMultiplier)
                    .mapToInt(BigDecimal::intValue)
                    .findFirst()
                    .orElse(0);

            if (sameSymbolsCount >= winCombinationCount) {
                reward = reward.add(BigDecimal.valueOf(winCombinationCount));
            }
        }

        return reward;
    }

    /**
     * Method to calculate the reward with a bonus
     *
     * @param winReward
     * @param appliedBonusSymbol
     * @return
     */
    private static BigDecimal getReward(BigDecimal winReward, String appliedBonusSymbol) {
        BigDecimal reward;
        Map<String, Integer> bonusSymbolsMap = new HashMap<>();
        bonusSymbolsMap.put("10x", 10);
        bonusSymbolsMap.put("5x", 5);
        bonusSymbolsMap.put("+1000", 1000);
        bonusSymbolsMap.put("+500", 500);
        bonusSymbolsMap.put("MISS", 0);
        bonusSymbolsMap.put(null, 0);

        Integer bonusValue = bonusSymbolsMap.get(appliedBonusSymbol);

        if (bonusValue < 100) {
            reward = winReward.multiply(BigDecimal.valueOf(bonusValue));
        } else {
            reward = winReward.add(BigDecimal.valueOf(bonusValue));
        }
        return reward;
    }

    /**
     * Utility method to create the randomly generated matrix
     *
     * @param bettingGame
     * @param bonusSymbols
     * @param standardSymbols
     * @param random
     * @param matrix
     * @return
     */
    private static List<List<String>> generateMatrix(BettingGame bettingGame, Set<String> bonusSymbols, List<String> standardSymbols, Random random, List<List<String>> matrix) {
        int columns = bettingGame.getColumns().orElse(4);
        int rows = bettingGame.getRows().orElse(4);

        List<String> matrixSymbols = new ArrayList<>(bonusSymbols);
        matrixSymbols.addAll(standardSymbols);

        for (int x = 0; x < columns; x++) {
            List<String> matrixValues = new ArrayList<>();
            for (int y = 0; y < rows; y++) {
                String randomSymbol = matrixSymbols.get(random.nextInt(matrixSymbols.size()));
                matrixValues.add(randomSymbol);
            }
            matrix.add(matrixValues);
        }

        return matrix;
    }
}
