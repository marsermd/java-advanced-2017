package ru.ifmo.ctddev.panin.webcrawler;

import info.kgeorgiy.java.advanced.crawler.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class WebCrawler implements Crawler
{
    private final Downloader downloader;
    private final ExecutorService downloaderService;
    private final ExecutorService urlExtractorService;
    private final int hostDownloadLimit;

    private HostScheduler hostScheduler;

    /**
     * Barier for all worker threads
     */
    private final Phaser barier = new Phaser();

    private DownloadResult result;

    public WebCrawler(Downloader downloader, int downloaders, int extractors, int perHost)
    {
        this.downloader = downloader;
        this.downloaderService = Executors.newFixedThreadPool(downloaders);
        this.urlExtractorService = Executors.newFixedThreadPool(extractors);
        this.hostDownloadLimit = perHost;
    }

    @Override
    public synchronized Result download(String url, int depth)
    {
        result = new DownloadResult();
        hostScheduler = new HostScheduler(hostDownloadLimit);

        barier.register();

        recursiveDownload(url, DepthCounter.newCounter(depth));

        barier.arriveAndAwaitAdvance();

        List<String> succesfulyVisitedPages = result.getVisitedWithoutError().collect(Collectors.toList());

        return new Result(succesfulyVisitedPages, result.getErrors());
    }

    @Override
    public void close()
    {
        urlExtractorService.shutdownNow();
        downloaderService.shutdownNow();
    }

    private void recursiveDownload(String url, DepthCounter counter)
    {
        if (result.isVisited(url))
        {
            return;
        }
        result.markAsVisited(url);

        String hostName;
        try
        {
            hostName = URLUtils.getHost(url);
        }
        catch (MalformedURLException e)
        {
            result.addError(url, e);
            return;
        }

        addDownloadTask(url, counter, hostName);
    }

    /**
     * add download task for given url
     * @param url url to extract from
     * @param counter recursion depth limiter
     * @param downloaded document by given url
     */
    private void addDownloadTask(String url, DepthCounter counter, String hostName)
    {
        Runnable download = () ->
        {
            try
            {
                Document downloaded = downloader.download(url);

                if (!counter.isFinished())
                {
                    addExtractorTask(url, counter, downloaded);
                }
            }
            catch (IOException e)
            {
                result.addError(url, e);
            }
            finally
            {
                hostScheduler.onFinishedDownloadTask(hostName, downloaderService);
                barier.arrive();
            }
        };

        barier.register();
        hostScheduler.submitOrScheduleDownload(hostName, downloaderService, download);
    }

    /**
     * Add extractor task for given document
     * @param url url to extract from
     * @param counter recursion depth limiter
     * @param downloaded document by given url
     */
    private void addExtractorTask(String url, DepthCounter counter, Document downloaded)
    {
        Runnable extractor = () ->
        {
            try
            {
                List<String> links = downloaded.extractLinks();
                for (String link : links)
                {
                    recursiveDownload(link, counter.next());
                }
            }
            catch (IOException e)
            {
                result.addError(url, e);
            }
            finally
            {
                barier.arrive();
            }
        };

        barier.register();
        urlExtractorService.submit(extractor);
    }

    public static void main(String[] args)
    {
        if (args.length < 1)
        {
            printUsage();
            return;
        }

        String url = args[0];

        int[] defaultArgs = new int[]
        {
            3, // depth
            3, // downloads
            1, // extractors
            3, // perHost
        };

        String[] userArgs = Arrays.copyOfRange(args, 1, args.length);
        if (userArgs.length > defaultArgs.length)
        {
            printUsage();
            return;
        }

        for (int i = 0; i < defaultArgs.length && i < userArgs.length; i++)
        {
            try
            {
                defaultArgs[i] = Integer.parseInt(userArgs[i]);
            }
            catch (NumberFormatException e)
            {
                printUsage();
                return;
            }
        }

        try (WebCrawler crawler = new WebCrawler(new CachingDownloader(), defaultArgs[1], defaultArgs[2], defaultArgs[3]);)
        {
            crawler.download(url, defaultArgs[0]);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return;
        }
    }

    private static void printUsage()
    {
        System.out.println("Usage:");
        System.out.println("WebCrawler url [depth [downloads [extractors [perHost]]]]");
    }
}