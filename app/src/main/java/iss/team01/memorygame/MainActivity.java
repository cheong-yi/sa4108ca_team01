package iss.team01.memorygame;

import androidx.appcompat.app.AppCompatActivity;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private Button scrapeBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        scrapeBtn = findViewById(R.id.scrapeBtn);

        // button to start scraper
        scrapeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // runs scraping task on a new thread
                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        // no output, instead it calls onTaskComplete at the end of the function
                        // which takes the imageUrls List
                        ScrapeImagesTask scrapeTask = new ScrapeImagesTask(new ScrapeImagesTask.TaskListener() {
                            @Override
                            public void onTaskComplete(List<String> imageUrls) {
                                // TODO: Process the list of image URLs here
                                // something like, dynamically add it to the layout with
                                // addView()?
                            }
                        });

                        scrapeTask.execute();
                    }
                }).start();
            }
        });
    }

    public class ScrapeImagesTask {

        private TaskListener listener;

        // notifies the MainActivity once the task is complete
        public ScrapeImagesTask(TaskListener listener) {
            this.listener = listener;
        }

        // some black magic about running it async, because you cant
        // run it in the main task, IDK don't ask me
        public void execute() {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    List<String> imageUrls = new ArrayList<>();

                    try {
                        Log.d("MainActivity", "Scraping images...");

                        // Fetch the HTML code of the website
                        // TODO: change it to take in user input
                        Document doc = Jsoup.connect("https://stocksnap.io/").get();

                        Log.d("MainActivity", "Scrape successful.");

                        // Select all .jpeg elements tagged with <img> on the page
                        Elements images = doc.select("img[src$=.jpg]");

                        // Narrow it down to the first 20
                        Elements first20Images = images.stream()
                                .limit(20)
                                .collect(Collectors.toCollection(Elements::new));

                        // for debugging, comment out if not needed
                        Log.d("MainActivity", "Number of unique images in first20: " + first20Images.size());
                        Log.d("MainActivity", "Adding images to list.");

                        // Iterate over the <img> elements and add their source URLs to the list
                        for (Element image : first20Images) {
                            String imageUrl = image.attr("src");
                            Log.d("MainActivity", imageUrl);
                            imageUrls.add(imageUrl);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    // for listener
                    if (listener != null) {
                        listener.onTaskComplete(imageUrls);
                    }
                }
            }).start();
        }

        public interface TaskListener {
            void onTaskComplete(List<String> imageUrls);
        }
    }
}