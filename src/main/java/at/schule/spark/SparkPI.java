package at.schule.spark;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SparkSession;

import java.util.ArrayList;
import java.util.List;

public class SparkPI {
    public static void main(String[] args) {
        SparkSession spark = SparkSession.builder()
                .appName("JavaSparkPi")
                .getOrCreate();

        int slices = 10;
        int n = 100000 * slices;
        List<Integer> l = new ArrayList<>(n);
        for (int i = 0; i < n; i++) { l.add(i); }

        JavaRDD<Integer> dataSet = new JavaSparkContext(spark.sparkContext()).parallelize(l, slices);

        long count = dataSet.filter(i -> {
            double x = Math.random() * 2 - 1;
            double y = Math.random() * 2 - 1;
            return x * x + y * y <= 1;
        }).count();

        System.out.println("Pi ist etwa: " + 4.0 * count / n);
        spark.stop();
    }
}

