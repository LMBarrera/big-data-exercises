package nearsoft.academy.bigdata.recommendation;

import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.ThresholdUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.UserBasedRecommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MovieRecommender {

    private int reviews;
    private long users;
    private int products;
    public UserBasedRecommender recommender;
    private HashMap<String, Integer> productTable = new HashMap();
    private HashMap<String, Long> userTable = new HashMap();
    private HashMap<Integer, String> iProductsTable = new HashMap();

    public MovieRecommender(String txt) throws Exception {
        readReviewsFile(txt);
    }

    private void readReviewsFile(String txt) throws Exception {
        DataModel model;
        UserSimilarity similarity;
        UserNeighborhood neighborhood;

        File file = new File(txt);
        BufferedReader br = new BufferedReader(new FileReader(file));
        FileWriter writer = new FileWriter("data.csv");
        BufferedWriter bw = new BufferedWriter(writer);
        String csv = "";

        for (String line = br.readLine(); line != null; line = br.readLine()) {
            if (line.startsWith("product/productId")) {
                String productId = line.split(" ")[1];
                this.reviews++;
                if (this.productTable.containsKey(productId)) {
                    csv = this.productTable.get(productId) + ",";
                } else {
                    this.productTable.put(productId, this.products);
                    this.iProductsTable.put(this.products, productId);
                    csv = this.products + ",";
                    this.products++;
                }
            } else if (line.startsWith("review/userId")) {
                String userId = line.split(" ")[1];
                if (this.userTable.containsKey(userId)) {
                    csv = this.userTable.get(userId) + "," + csv;
                } else {
                    this.userTable.put(userId, this.users);
                    csv = this.users + "," + csv;
                    this.users++;
                }
            } else if (line.startsWith("review/score")) {
                String reviewScore = line.split(" ")[1];
                csv += reviewScore + "\n";
                bw.write(csv);
            }
        }
        bw.close();
        model = new FileDataModel(new File("data.csv"));
        similarity = new PearsonCorrelationSimilarity(model);
        neighborhood = new ThresholdUserNeighborhood(0.1, similarity, model);
        this.recommender = new GenericUserBasedRecommender(model, neighborhood, similarity);
    }

    public int getTotalReviews() {
        return this.reviews;
    }

    public int getTotalProducts() {
        return this.products;
    }

    public long getTotalUsers() {
        return this.users;
    }

    public List<String> getRecommendationsForUser(String user) throws Exception {
        List<String> results = new ArrayList();

        long id = userTable.get(user);
        List<RecommendedItem> recommendations = recommender.recommend(id, 3);

        for (RecommendedItem recommendation : recommendations) {
            results.add(iProductsTable.get((int) recommendation.getItemID()));
        }

        return results;
    }
}