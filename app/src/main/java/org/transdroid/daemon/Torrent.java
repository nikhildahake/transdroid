/*
 *	This file is part of Transdroid <http://www.transdroid.org>
 *
 *	Transdroid is free software: you can redistribute it and/or modify
 *	it under the terms of the GNU General Public License as published by
 *	the Free Software Foundation, either version 3 of the License, or
 *	(at your option) any later version.
 *
 *	Transdroid is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *	GNU General Public License for more details.
 *
 *	You should have received a copy of the GNU General Public License
 *	along with Transdroid.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.transdroid.daemon;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Calendar;
import java.util.Date;

/**
 * Represents a torrent on a server daemon.
 *
 * @author erickok
 */
public final class Torrent implements Parcelable, Comparable<Torrent>, Finishable {

    public static final Parcelable.Creator<Torrent> CREATOR = new Parcelable.Creator<Torrent>() {
        public Torrent createFromParcel(Parcel in) {
            return new Torrent(in);
        }

        public Torrent[] newArray(int size) {
            return new Torrent[size];
        }
    };
    final private long id;
    final private String hash;
    final private String name;
    final private int rateDownload;
    final private int rateUpload;
    final private int seedersConnected;
    final private int seedersKnown;
    final private int leechersConnected;
    final private int leechersKnown;
    final private int eta;
    final private long downloadedEver;
    final private long uploadedEver;
    final private long totalSize;
    final private float partDone;
    final private float available;
    final private Date dateAdded;
    final private Date dateDone;
    final private String error;
    final private Daemon daemon;
    private TorrentStatus statusCode;
    private String locationDir;
    private String label;
    private boolean sequentialDownload;
    private boolean firstLastPieceDownload;
    private int numberOfTrackers;

    private Torrent(Parcel in) {
        this.id = in.readLong();
        this.hash = in.readString();
        this.name = in.readString();
        this.statusCode = TorrentStatus.getStatus(in.readInt());
        this.locationDir = in.readString();

        this.rateDownload = in.readInt();
        this.rateUpload = in.readInt();
        this.seedersConnected = in.readInt();
        this.seedersKnown = in.readInt();
        this.leechersConnected = in.readInt();
        this.leechersKnown = in.readInt();
        this.eta = in.readInt();

        this.downloadedEver = in.readLong();
        this.uploadedEver = in.readLong();
        this.totalSize = in.readLong();
        this.partDone = in.readFloat();
        this.available = in.readFloat();
        this.label = in.readString();
        this.sequentialDownload = in.readByte() != 0;
        this.firstLastPieceDownload = in.readByte() != 0;

        long lDateAdded = in.readLong();
        this.dateAdded = (lDateAdded == -1) ? null : new Date(lDateAdded);
        long lDateDone = in.readLong();
        this.dateDone = (lDateDone == -1) ? null : new Date(lDateDone);
        this.error = in.readString();
        this.daemon = Daemon.valueOf(in.readString());
    }

    public Torrent(long id, String hash, String name, TorrentStatus statusCode, String locationDir, int rateDownload,
                   int rateUpload, int seedersConnected, int seedersKnown, int leechersConnected, int leechersKnown, int eta,
                   long downloadedEver, long uploadedEver, long totalSize, float partDone, float available, String label,
                   Date dateAdded, Date realDateDone, String error, Daemon daemon) {
        this.id = id;
        this.hash = hash;
        this.name = name;
        this.statusCode = statusCode;
        this.locationDir = locationDir;

        this.rateDownload = rateDownload;
        this.rateUpload = rateUpload;
        this.seedersConnected = seedersConnected;
        this.seedersKnown = seedersKnown;
        this.leechersConnected = leechersConnected;
        this.leechersKnown = leechersKnown;
        this.eta = eta;

        this.downloadedEver = downloadedEver;
        this.uploadedEver = uploadedEver;
        this.totalSize = totalSize;
        this.partDone = partDone;
        this.available = available;
        this.label = label;
        this.sequentialDownload = false;
        this.firstLastPieceDownload = false;

        this.dateAdded = dateAdded;
        if (realDateDone != null) {
            this.dateDone = realDateDone;
        } else {
            if (this.partDone == 1) {
                // Finished but no finished date: set so move to bottom of list
                Calendar cal = Calendar.getInstance();
                cal.clear();
                cal.set(1900, Calendar.DECEMBER, 31);
                this.dateDone = cal.getTime();
            } else if (eta == -1 || eta == -2) {
                // UNknown eta: move to the top of the list
                this.dateDone = new Date(Long.MAX_VALUE);
            } else {
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.SECOND, eta);
                this.dateDone = cal.getTime();
            }
        }
        this.error = error;
        this.daemon = daemon;
    }

    Torrent(Builder builder) {
        this.id = builder.id;
        this.hash = builder.hash;
        this.name = builder.name;
        this.statusCode = builder.statusCode;
        this.locationDir = builder.locationDir;

        this.rateDownload = builder.rateDownload;
        this.rateUpload = builder.rateUpload;
        this.seedersConnected = builder.seedersConnected;
        this.seedersKnown = builder.seedersKnown;
        this.leechersConnected = builder.leechersConnected;
        this.leechersKnown = builder.leechersKnown;
        this.eta = builder.eta;

        this.downloadedEver = builder.downloadedEver;
        this.uploadedEver = builder.uploadedEver;
        this.totalSize = builder.totalSize;
        this.partDone = builder.partDone;
        this.available = builder.available;
        this.label = builder.label;

        this.dateAdded = builder.dateAdded;
        if (builder.realDateDone != null) {
            this.dateDone = builder.realDateDone;
        } else {
            if (this.partDone == 1) {
                // Finished but no finished date: set so move to bottom of list
                Calendar cal = Calendar.getInstance();
                cal.clear();
                cal.set(1900, Calendar.DECEMBER, 31);
                this.dateDone = cal.getTime();
            } else if (eta == -1 || eta == -2) {
                // UNknown eta: move to the top of the list
                this.dateDone = new Date(Long.MAX_VALUE);
            } else {
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.SECOND, eta);
                this.dateDone = cal.getTime();
            }
        }
        this.error = builder.error;
        this.daemon = builder.daemon;
        this.sequentialDownload = builder.sequentialDownload;
        this.firstLastPieceDownload = builder.firstLastPieceDownload;

    }

    public String getName() {
        return name;
    }

    public TorrentStatus getStatusCode() {
        return statusCode;
    }

    public String getLocationDir() {
        return locationDir;
    }

    public int getRateDownload() {
        return rateDownload;
    }

    public int getRateUpload() {
        return rateUpload;
    }

    public int getSeedersConnected() {
        return seedersConnected;
    }

    public int getSeedersKnown() {
        return seedersKnown;
    }

    public int getLeechersConnected() {
        return leechersConnected;
    }

    public int getLeechersKnown() {
        return leechersKnown;
    }

    public int getEta() {
        return eta;
    }

    public long getDownloadedEver() {
        return downloadedEver;
    }

    public long getUploadedEver() {
        return uploadedEver;
    }

    public long getTotalSize() {
        return totalSize;
    }

    public float getPartDone() {
        return partDone;
    }

    public float getAvailability() {
        return available;
    }

    public String getLabelName() {
        return label;
    }

    public boolean isSequentiallyDownloading() {
        return sequentialDownload;
    }

    public boolean isDownloadingFirstLastPieceFirst() {
        return firstLastPieceDownload;
    }

    public Date getDateAdded() {
        return dateAdded;
    }

    public Date getDateDone() {
        return dateDone;
    }

    public String getError() {
        return error;
    }

    public Daemon getDaemon() {
        return daemon;
    }

    public int getNumberOfTrackers() {
        return this.numberOfTrackers;
    }

    public void setNumberOfTrackers(int numberOfTrackers) {
        this.numberOfTrackers = numberOfTrackers;
    }

    /**
     * Returns the torrent-specific ID, which is the torrent's hash or (if not available) the long number
     *
     * @return The torrent's (session-transient) unique ID
     */
    public String getUniqueID() {
        if (this.hash == null) {
            return "" + this.id;
        } else {
            return this.hash;
        }
    }

    /**
     * Gives the upload/download seed ratio.
     *
     * @return The ratio in range [0,r]
     */
    public double getRatio() {
        return ((double) uploadedEver) / ((double) downloadedEver);
    }

    /**
     * Gives the percentage of the download that is completed
     *
     * @return The downloaded percentage in range [0,1]
     */
    public float getDownloadedPercentage() {
        return partDone;
    }

    /**
     * Returns whether this torrents is actively downloading or not.
     *
     * @param dormantAsInactive If true, dormant (0KB/s, so no data transfer) torrents are never actively downloading
     * @return True if this torrent is to be treated as being in a downloading state, that is, it is trying to finish a
     * download
     */
    public boolean isDownloading(boolean dormantAsInactive) {
        return statusCode == TorrentStatus.Downloading && (!dormantAsInactive || rateDownload > 0);
    }

    /**
     * Returns whether this torrents is actively seeding or not.
     *
     * @param dormantAsInactive If true, dormant (0KB/s, so no data transfer) torrents are never actively seeding
     * @return True if this torrent is to be treated as being in a seeding state, that is, it is sending data to
     * leechers
     */
    public boolean isSeeding(boolean dormantAsInactive) {
        return statusCode == TorrentStatus.Seeding && (!dormantAsInactive || rateUpload > 0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isStarted() {
        return partDone > 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isFinished() {
        return partDone >= 1;
    }

    /**
     * Indicates if the torrent can be paused at this moment
     *
     * @return If it can be paused
     */
    public boolean canPause() {
        // Can pause when it is downloading or seeding
        return statusCode == TorrentStatus.Downloading || statusCode == TorrentStatus.Seeding || statusCode == TorrentStatus.Queued;
    }

    /**
     * Indicates whether the torrent can be resumed
     *
     * @return If it can be resumed
     */
    public boolean canResume() {
        // Can resume when it is paused
        return statusCode == TorrentStatus.Paused;
    }

    /**
     * Indicates if the torrent can be started at this moment
     *
     * @return If it can be started
     */
    public boolean canStart() {
        // Can start when it is queued
        return statusCode == TorrentStatus.Queued;
    }

    /**
     * Indicates whether the torrent can be stopped
     *
     * @return If it can be stopped
     */
    public boolean canStop() {
        // Can stop when it is downloading or seeding or paused
        return statusCode == TorrentStatus.Downloading || statusCode == TorrentStatus.Seeding
                || statusCode == TorrentStatus.Paused;
    }

    public void mimicResume() {
        if (getDownloadedPercentage() >= 1) {
            statusCode = TorrentStatus.Seeding;
        } else {
            statusCode = TorrentStatus.Downloading;
        }
    }

    public void mimicPause() {
        statusCode = TorrentStatus.Paused;
    }

    public void mimicStart() {
        if (getDownloadedPercentage() >= 1) {
            statusCode = TorrentStatus.Seeding;
        } else {
            statusCode = TorrentStatus.Downloading;
        }
    }

    public void mimicStop() {
        statusCode = TorrentStatus.Queued;
    }

    public void mimicNewLabel(String newLabel) {
        label = newLabel;
    }

    public void mimicSequentialDownload(boolean sequentialDownload) {
        this.sequentialDownload = sequentialDownload;
    }

    public void mimicFirstLastPieceDownload(boolean firstLastPieceDownload) {
        this.firstLastPieceDownload = firstLastPieceDownload;
    }

    public void mimicCheckingStatus() {
        statusCode = TorrentStatus.Checking;
    }

    public void mimicNewLocation(String newLocation) {
        locationDir = newLocation;
    }

    @Override
    public String toString() {
        // (HASH_OR_ID) NAME
        return "(" + ((hash != null) ? hash : String.valueOf(id)) + ") " + name;
    }

    @Override
    public int compareTo(Torrent another) {
        // Compare torrent objects on their name (used for sorting only!)
        return name.compareTo(another.getName());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(hash);
        dest.writeString(name);
        dest.writeInt(statusCode.getCode());
        dest.writeString(locationDir);

        dest.writeInt(rateDownload);
        dest.writeInt(rateUpload);
        dest.writeInt(seedersConnected);
        dest.writeInt(seedersKnown);
        dest.writeInt(leechersConnected);
        dest.writeInt(leechersKnown);
        dest.writeInt(eta);

        dest.writeLong(downloadedEver);
        dest.writeLong(uploadedEver);
        dest.writeLong(totalSize);
        dest.writeFloat(partDone);
        dest.writeFloat(available);
        dest.writeString(label);
        dest.writeByte((byte) (sequentialDownload ? 1 : 0));
        dest.writeByte((byte) (firstLastPieceDownload ? 1 : 0));

        dest.writeLong((dateAdded == null) ? -1 : dateAdded.getTime());
        dest.writeLong((dateDone == null) ? -1 : dateDone.getTime());
        dest.writeString(error);
        dest.writeString(daemon.name());
    }

    public static class Builder {

        private long id;
        private String hash;
        private String name;
        private TorrentStatus statusCode;
        private String locationDir;
        private int rateDownload;
        private int rateUpload;
        private int seedersConnected;
        private int seedersKnown;
        private int leechersConnected;
        private int leechersKnown;
        private int eta;
        private long downloadedEver;
        private long uploadedEver;
        private long totalSize;
        private float partDone;
        private float available;
        private String label;
        private Date dateAdded;
        private Date realDateDone;
        private String error;
        private Daemon daemon;
        private boolean sequentialDownload;
        private boolean firstLastPieceDownload;

        public Builder setId(long id) {
            this.id = id;
            return this;
        }

        public Builder setHash(String hash) {
            this.hash = hash;
            return this;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setStatusCode(TorrentStatus statusCode) {
            this.statusCode = statusCode;
            return this;
        }

        public Builder setLocationDir(String locationDir) {
            this.locationDir = locationDir;
            return this;
        }

        public Builder setRateDownload(int rateDownload) {
            this.rateDownload = rateDownload;
            return this;
        }

        public Builder setRateUpload(int rateUpload) {
            this.rateUpload = rateUpload;
            return this;
        }

        public Builder setSeedersConnected(int seedersConnected) {
            this.seedersConnected = seedersConnected;
            return this;
        }

        public Builder setSeedersKnown(int seedersKnown) {
            this.seedersKnown = seedersKnown;
            return this;
        }

        public Builder setLeechersConnected(int leechersConnected) {
            this.leechersConnected = leechersConnected;
            return this;
        }

        public Builder setLeechersKnown(int leechersKnown) {
            this.leechersKnown = leechersKnown;
            return this;
        }

        public Builder setEta(int eta) {
            this.eta = eta;
            return this;
        }

        public Builder setDownloadedEver(long downloadedEver) {
            this.downloadedEver = downloadedEver;
            return this;
        }

        public Builder setUploadedEver(long uploadedEver) {
            this.uploadedEver = uploadedEver;
            return this;
        }

        public Builder setTotalSize(long totalSize) {
            this.totalSize = totalSize;
            return this;
        }

        public Builder setPartDone(float partDone) {
            this.partDone = partDone;
            return this;
        }

        public Builder setAvailable(float available) {
            this.available = available;
            return this;
        }

        public Builder setLabel(String label) {
            this.label = label;
            return this;
        }

        public Builder setDateAdded(Date dateAdded) {
            this.dateAdded = dateAdded;
            return this;
        }

        public Builder setRealDateDone(Date realDateDone) {
            this.realDateDone = realDateDone;
            return this;
        }

        public Builder setError(String error) {
            this.error = error;
            return this;
        }

        public Builder setDaemon(Daemon daemon) {
            this.daemon = daemon;
            return this;
        }

        public Builder setSequentialDownload(boolean sequentialDownload) {
            this.sequentialDownload = sequentialDownload;
            return this;
        }

        public Builder setFirstLastPieceDownload(boolean firstLastPieceDownload) {
            this.firstLastPieceDownload = firstLastPieceDownload;
            return this;
        }

        public Torrent createTorrent() {
            return new Torrent(this);
        }
    }
}
