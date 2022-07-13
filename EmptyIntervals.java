import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;
import java.util.stream.Collectors;

public class EmptyIntervals {
    public static List<List<String>> getEmptyIntervals(String filePath) {
        BufferedReader bufferedReader;
        try {
            bufferedReader = new BufferedReader(new FileReader(filePath));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e); //TODO
        }

        //get list of intervals, then sort them chronologically according to startdate
        List<String[]> intervals =
                bufferedReader.lines()                                      //separate lines
                              .filter(line -> line.contains("<bar"))        //select lines with "<bar"
                              .map(bar -> {                                 //extract enddate and startdate
                                  String[] barData = bar.split("\" ");
                                  return Arrays.stream(barData)
                                               .filter(datum -> datum.startsWith("enddate") || datum.startsWith("startdate"))
                                               .map(timeStamp -> timeStamp.replace("\"", ""))
                                               .sorted(Comparator.reverseOrder())
                                               .toArray(String[]::new);
                              })
                              .distinct()
                              .sorted(Comparator.comparing(intvl -> intvl[1]))
                              .collect(Collectors.toList());
        System.out.println("sorted: " + intervals);

        int prevIntervalIndex = 0;
        for (int i = 1; i < intervals.size(); i++) {
            //Note: prevInterval's start is earlier than or the same as currentInterval's start
            String[] prevInterval = intervals.get(prevIntervalIndex);
            String prevIntervalEndTimeStamp = prevInterval[1].split("=")[1];
            String[] currentInterval = intervals.get(i);
            String currentIntervalStartTimeStamp = currentInterval[0].split("=")[1];
            if (prevIntervalEndTimeStamp.compareTo(currentIntervalStartTimeStamp) >= 0) {
                //prevInterval's end is later than or the same as currentInterval's start
                //consolidate currentInterval into latestNonOverlappingInterval
                prevInterval[1] = currentInterval[1];
                intervals.remove(i);
                i--;
            } else {
                //prevInterval's end is earlier than currentInterval's start
                //update prevInterval to currentInterval
                prevIntervalIndex = i;
            }
        }
        System.out.println("consolidated: " + intervals);

        List<String> timeStamps = intervals.stream()
                                           .flatMap(Arrays::stream)
                                           .collect(Collectors.toList());
        List<List<String>> emptyIntervals = new ArrayList<>();
        for (int i = 0; i < timeStamps.size() - 1; i++) {
            String timeStamp = timeStamps.get(i);
            if (timeStamp.startsWith("enddate")) {
                List<String> emptyInterval = new ArrayList<>();
                emptyInterval.add(timeStamp);
                emptyInterval.add(timeStamps.get(i+1));
                emptyIntervals.add(emptyInterval);
            }
        }
        System.out.println("empty: " + emptyIntervals);

        return emptyIntervals;
    }
}