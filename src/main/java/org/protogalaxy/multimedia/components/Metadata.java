package org.protogalaxy.multimedia.components;

import static org.bytedeco.javacpp.avcodec.*;
import static org.bytedeco.javacpp.avformat.*;
import static org.bytedeco.javacpp.avutil.*;
import static org.bytedeco.javacpp.swscale.*;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.DoublePointer;
import org.protogalaxy.multimedia.MusicContainer;

import java.awt.image.BufferedImage;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

//TODO: Cover

/**
 *
 */
public class Metadata {
    private String title;
    private String album;
    private String artist;
    private String album_artist;
    private String composer;
    private String performer;
    private String date;
    private String track;
    private int disc;
    private String genre;
    private String publisher;
    private String comment;
    private long duration_milliseconds;
    private long duration_seconds;
    private String duration_formatted;
    private long bitrate_raw;
    private String bitrate_formatted;
    private int sampleRate_raw;
    private String sampleRate_formatted;
    private int bitDepth_raw;
    private String bitDepth_formatted;
    private long size_kilobytes_raw;
    private String size_kilobytes_formatted;
    private double size_megabytes_raw;
    private String size_megabytes_formatted;
    private BufferedImage cover;

    private String
            METADATA_TITLE = "title",
            METADATA_ALBUM = "album",
            METADATA_ARTIST = "artist",
            METADATA_ALBUMARTIST = "album_artist",
            METADATA_COMPOSER = "composer",
            METADATA_PERFORMER = "performer",
            METADATA_DATE = "date",
            METADATA_TRACK = "track",
            METADATA_DISC = "disc",
            METADATA_GENRE = "genre",
            METADATA_PUBLISHER = "publisher",
            METADATA_COMMENT = "comment",
            METADATA_DURATION = "duration",
            METADATA_BITRATE = "bitrate",
            METADATA_SAMPLERATE = "sample_rate",
            METADATA_BITDEPTH = "bit_depth",
            METADATA_SIZE = "size",
            METADATA_COVER = "cover";

    private ArrayList<String> metadataStandardArray = new ArrayList<>(Arrays.asList(
            METADATA_TITLE,
            METADATA_ALBUM,
            METADATA_ARTIST,
            METADATA_ALBUMARTIST,
            METADATA_COMPOSER,
            METADATA_PERFORMER,
            METADATA_DATE,
            METADATA_TRACK,
            METADATA_DISC,
            METADATA_GENRE,
            METADATA_PUBLISHER,
            METADATA_COMMENT
    ));
    private ArrayList<String> metadataFullArray = new ArrayList<>(Arrays.asList(
            METADATA_TITLE,
            METADATA_ALBUM,
            METADATA_ARTIST,
            METADATA_ALBUMARTIST,
            METADATA_COMPOSER,
            METADATA_PERFORMER,
            METADATA_DATE,
            METADATA_TRACK,
            METADATA_DISC,
            METADATA_GENRE,
            METADATA_PUBLISHER,
            METADATA_COMMENT,
            METADATA_DURATION,
            METADATA_BITRATE,
            METADATA_SAMPLERATE,
            METADATA_BITDEPTH,
            METADATA_SIZE,
            METADATA_COVER
    ));
    private Map<String, String> metadataStandardMap = new HashMap<>();
    private Map<String, String> metadataFullMap = new HashMap<>();

    /**
     * Initialize the metadata object
     *
     * @param container music file container
     */
    public Metadata(MusicContainer container) {
        AVDictionaryEntry entry = null;
        Map<String, String> metadataCacheMap = new HashMap<>();
        while ((entry = av_dict_get(container.getAvFormatContext().metadata(), "", entry, AV_DICT_IGNORE_SUFFIX)) != null) {
            metadataCacheMap.put(entry.key().getString(), entry.value().getString());
        }
        for (String metadata : metadataStandardArray) {
            metadataStandardMap.put(metadata, metadataCacheMap.get(metadata));
        }
        for (String metadata : metadataStandardArray) {
            metadataFullMap.put(metadata, metadataCacheMap.get(metadata));
        }
        metadataFullMap.put(METADATA_DURATION, formatDuration(container.getAvFormatContext().duration()));
        metadataFullMap.put(METADATA_BITRATE, formatBitrate(container.getAvFormatContext().streams(0).codecpar().bit_rate()));
        metadataFullMap.put(METADATA_SAMPLERATE, formatSampleRate(container.getAvFormatContext().streams(0).codecpar().sample_rate()));
        metadataFullMap.put(METADATA_BITDEPTH, formatBitDepth(container.getAvFormatContext().streams(0).codecpar().bits_per_raw_sample()));
        metadataFullMap.put(METADATA_SIZE, formatSizeMegabytes(container.getPath().toFile().length()));
        this.duration_milliseconds = 1000 * (container.getAvFormatContext().duration() / AV_TIME_BASE) + (1000 * (container.getAvFormatContext().duration() % AV_TIME_BASE)) / AV_TIME_BASE;
        this.duration_seconds = (container.getAvFormatContext().duration() / AV_TIME_BASE);
        this.duration_formatted = formatDuration(container.getAvFormatContext().duration());
        this.bitrate_raw = container.getAvFormatContext().streams(0).codecpar().bit_rate();
        this.bitrate_formatted = formatBitrate(container.getAvFormatContext().streams(0).codecpar().bit_rate());
        this.sampleRate_raw = container.getAvFormatContext().streams(0).codecpar().sample_rate();
        this.sampleRate_formatted = formatSampleRate(container.getAvFormatContext().streams(0).codecpar().sample_rate());
        this.bitDepth_raw = container.getAvFormatContext().streams(0).codecpar().bits_per_raw_sample();
        this.bitDepth_formatted = formatBitDepth(container.getAvFormatContext().streams(0).codecpar().bits_per_raw_sample());
        this.size_kilobytes_raw = container.getPath().toFile().length() / 1024;
        this.size_kilobytes_formatted = formatSizeKilobytes(container.getPath().toFile().length());
        this.size_megabytes_raw = container.getPath().toFile().length() / 1024 / 1024;
        this.size_megabytes_formatted = formatSizeMegabytes(container.getPath().toFile().length());
    }

    public void write(MusicContainer container) {

    }

    /**
     * Read cover from music container
     *
     * @param container music file container
     * @return buffered music cover
     */
    private BufferedImage readCover(MusicContainer container) {
        av_register_all();
        BufferedImage cover;
        AVFormatContext avFormatContext = container.getAvFormatContext();
        AVCodecParameters avCodecParameters = avFormatContext.streams(0).codecpar(); //Get codec parameters
        AVCodec avCodec = avcodec_find_decoder(avCodecParameters.codec_id()); //Find the decoder
        AVCodecContext avCodecContext = avcodec_alloc_context3(avCodec); //Begin to decode video stream
        AVFrame avFrame = av_frame_alloc();
        AVFrame avFrameRGB = av_frame_alloc();
        AVPacket avPacket = av_packet_alloc();
        SwsContext swsContext = sws_getContext(avCodecParameters.width(), avCodecParameters.height(), avCodecParameters.format(),
                                               avCodecParameters.width(), avCodecParameters.height(), avCodecParameters.format(),
                                               SWS_BILINEAR, null, null, (DoublePointer) null);
        avcodec_open2(avCodecContext, avCodec, (AVDictionary) null);
        avcodec_receive_frame(avCodecContext, avFrame);
        av_image_fill_arrays(avFrameRGB.data(), avFrame.linesize(), avFrame.data(1), AV_PIX_FMT_RGB24, avCodecParameters.width(), avCodecParameters.height(), 1);
        sws_scale(swsContext, avFrame.data(), avFrame.linesize(), 0, avCodecParameters.height(), avFrameRGB.data(), avFrameRGB.linesize());
        cover = saveFrame(avFrameRGB, avCodecParameters.width(), avCodecParameters.height());
        //Free the packet
        av_packet_free(avPacket);
        //Close the codec
        avcodec_close(avCodecContext);
        //Free the avCodecContext
        avcodec_free_context(avCodecContext);
        //Free the avCodecParameters
        avcodec_parameters_free(avCodecParameters);

        return cover;
    }

    /**
     * Format track duration
     *
     * @param duration duration of the track
     * @return formatted duration of the track
     */
    private String formatDuration(long duration) {
        long secs, us;
        secs = (duration / AV_TIME_BASE);
        us = (1000 * (duration % AV_TIME_BASE)) / AV_TIME_BASE;
        long totalus = 1000 * secs + us;
        SimpleDateFormat format = new SimpleDateFormat("mm:ss:SSS");
        return format.format(totalus);
    }

    /**
     * Format track bitrate
     *
     * @param bitrate bitrate of the track
     * @return formatted bitrate
     */
    private String formatBitrate(long bitrate) {
        return String.valueOf(bitrate / 1000) + "kbps";
    }

    /**
     * Format track sample rate
     *
     * @param sampleRate sample rate of the track
     * @return formatted sample rate
     */
    private String formatSampleRate(int sampleRate) {
        return String.valueOf(sampleRate) + "Hz";
    }

    /**
     * Format track bit depth
     *
     * @param bitDepth bit depth of the track
     * @return formatted bit depth
     */
    private String formatBitDepth(int bitDepth) {
        return String.valueOf(bitDepth) + "Bit";
    }

    /**
     * Format size of the file in kilobyte
     *
     * @param size size of the file
     * @return formatted size of the file
     */
    private String formatSizeKilobytes(long size) {
        size = size / 1024;
        return String.valueOf(size) + "KB";
    }

    /**
     * Format size of the file in megabyte
     *
     * @param size size of the file
     * @return formatted size of the file
     */
    private String formatSizeMegabytes(long size) {
        size = size / 1024 / 1024 * 100;
        return String.valueOf((size / 100)) + "." + String.valueOf((size % 100)) + "MB";
    }

    /**
     * Save frame into memory
     *
     * @param avFrame saved frame
     * @param width   width of the image
     * @param height  height of the frame
     * @return buffered music cover
     */
    private BufferedImage saveFrame(AVFrame avFrame, int width, int height) {
        return null;//TODO content
    }

    /**
     * Get standard metadata name list
     *
     * @return string list of the standard metadata name
     */
    public ArrayList<String> getMetadataStandardArray() {
        return metadataStandardArray;
    }

    /**
     * Get all metadata name list
     *
     * @return string list of all metadata name
     */
    public ArrayList<String> getMetadataFullArray() {
        return metadataFullArray;
    }

    /**
     * Get standard metadata map
     *
     * @return map of the standard metadata
     */
    public Map<String, String> getMetadataStandardMap() {
        return metadataStandardMap;
    }

    /**
     * Get full metadata map
     *
     * @return map of all metadata
     */
    public Map<String, String> getMetadataFullMap() {
        return metadataFullMap;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbum_artist() {
        return album_artist;
    }

    public void setAlbum_artist(String album_artist) {
        this.album_artist = album_artist;
    }

    public String getComposer() {
        return composer;
    }

    public void setComposer(String composer) {
        this.composer = composer;
    }

    public String getPerformer() {
        return performer;
    }

    public void setPerformer(String performer) {
        this.performer = performer;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTrack() {
        return track;
    }

    public void setTrack(String track) {
        this.track = track;
    }

    public int getDisc() {
        return disc;
    }

    public void setDisc(int disc) {
        this.disc = disc;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public long getDuration_milliseconds() {
        return duration_milliseconds;
    }

    public long getDuration_seconds() {
        return duration_seconds;
    }

    public String getDuration_formatted() {
        return duration_formatted;
    }

    public long getBitrate_raw() {
        return bitrate_raw;
    }

    public String getBitrate_formatted() {
        return bitrate_formatted;
    }

    public int getSampleRate_raw() {
        return sampleRate_raw;
    }

    public String getSampleRate_formatted() {
        return sampleRate_formatted;
    }

    public int getBitDepth_raw() {
        return bitDepth_raw;
    }

    public String getBitDepth_formatted() {
        return bitDepth_formatted;
    }

    public long getSize_kilobytes_raw() {
        return size_kilobytes_raw;
    }

    public String getSize_kilobytes_formatted() {
        return size_kilobytes_formatted;
    }

    public double getSize_megabytes_raw() {
        return size_megabytes_raw;
    }

    public String getSize_megabytes_formatted() {
        return size_megabytes_formatted;
    }

    public BufferedImage getCover() {
        return cover;
    }

    public void setCover(BufferedImage cover) {
        this.cover = cover;
    }
}
