package com.company.SocialNetwork.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

class SlugGeneratorTest {

    private SlugGenerator slugGenerator;

    @BeforeEach
    public void setUp() {
        slugGenerator = new SlugGenerator();
    }

    @Test
    public void shouldNotGenerateDuplicatedSlug() throws InterruptedException {
        String[] slugs = new String[1000];

        CountDownLatch latch = new CountDownLatch(1000);
        ExecutorService executorService = Executors.newFixedThreadPool(10);

        for (int i = 0; i < 1000; i++) {
            int finalI = i;
            executorService.submit(() -> {
                try {
                    String slug = slugGenerator.generateSlug();
                    slugs[finalI] = slug;
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        int deduplicateCount = new HashSet<>(List.of(slugs)).size();
        assertThat(deduplicateCount).isEqualTo(1000);
    }
}