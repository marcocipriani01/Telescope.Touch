/*
 * Copyright 2021 Marco Cipriani (@marcocipriani01)
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.marcocipriani01.telescopetouch.layers;

import android.content.res.Resources;
import android.text.format.DateFormat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.EnumSet;
import java.util.GregorianCalendar;
import java.util.List;

import io.github.marcocipriani01.telescopetouch.R;
import io.github.marcocipriani01.telescopetouch.astronomy.GeocentricCoordinates;
import io.github.marcocipriani01.telescopetouch.astronomy.TimeUtils;
import io.github.marcocipriani01.telescopetouch.control.AstronomerModel;
import io.github.marcocipriani01.telescopetouch.renderer.RendererObjectManager.UpdateType;
import io.github.marcocipriani01.telescopetouch.source.AstronomicalSource;
import io.github.marcocipriani01.telescopetouch.source.ImageSource;
import io.github.marcocipriani01.telescopetouch.source.TextSource;

/**
 * A {@link Layer} to show well-known meteor showers.
 *
 * @author John Taylor
 */
public class MeteorShowerLayer extends AbstractLayer {

    public static final int DEPTH_ORDER = 50;
    public static final String PREFERENCE_ID = "source_provider.6";
    private static final int ANY_OLD_YEAR = 20000;
    /**
     * Number of meteors per hour for the larger graphic
     */
    private static final double METEOR_THRESHOLD_PER_HR = 10;
    private final AstronomerModel model;
    private final List<Shower> showers = new ArrayList<>();

    public MeteorShowerLayer(AstronomerModel model, Resources resources) {
        super(resources, true);
        this.model = model;
        initializeShowers();
    }

    private void initializeShowers() {
        // A list of all the meteor showers with > 10 per hour
        // Source: http://www.imo.net/calendar/2011#table5
        // Note the zero-based month. 10=November
        // Actual start for Quadrantids is December 28 - but we can't cross a year boundary.
        showers.add(new Shower(
                R.string.quadrantids, GeocentricCoordinates.getInstance(230, 49),
                new GregorianCalendar(ANY_OLD_YEAR, 0, 1),
                new GregorianCalendar(ANY_OLD_YEAR, 0, 4),
                new GregorianCalendar(ANY_OLD_YEAR, 0, 12),
                120));
        showers.add(new Shower(
                R.string.lyrids, GeocentricCoordinates.getInstance(271, 34),
                new GregorianCalendar(ANY_OLD_YEAR, 3, 16),
                new GregorianCalendar(ANY_OLD_YEAR, 3, 22),
                new GregorianCalendar(ANY_OLD_YEAR, 3, 25),
                18));
        showers.add(new Shower(
                R.string.aquariids, GeocentricCoordinates.getInstance(338, -1),
                new GregorianCalendar(ANY_OLD_YEAR, 3, 19),
                new GregorianCalendar(ANY_OLD_YEAR, 4, 6),
                new GregorianCalendar(ANY_OLD_YEAR, 4, 28),
                70));
        showers.add(new Shower(
                R.string.deltaaquariids, GeocentricCoordinates.getInstance(340, -16),
                new GregorianCalendar(ANY_OLD_YEAR, 6, 12),
                new GregorianCalendar(ANY_OLD_YEAR, 6, 30),
                new GregorianCalendar(ANY_OLD_YEAR, 7, 23),
                16));
        showers.add(new Shower(
                R.string.perseids, GeocentricCoordinates.getInstance(48, 58),
                new GregorianCalendar(ANY_OLD_YEAR, 6, 17),
                new GregorianCalendar(ANY_OLD_YEAR, 7, 13),
                new GregorianCalendar(ANY_OLD_YEAR, 7, 24),
                100));
        showers.add(new Shower(
                R.string.orionids, GeocentricCoordinates.getInstance(95, 16),
                new GregorianCalendar(ANY_OLD_YEAR, 9, 2),
                new GregorianCalendar(ANY_OLD_YEAR, 9, 21),
                new GregorianCalendar(ANY_OLD_YEAR, 10, 7),
                25));
        showers.add(new Shower(
                R.string.leonids, GeocentricCoordinates.getInstance(152, 22),
                new GregorianCalendar(ANY_OLD_YEAR, 10, 6),
                new GregorianCalendar(ANY_OLD_YEAR, 10, 18),
                new GregorianCalendar(ANY_OLD_YEAR, 10, 30),
                20));
        showers.add(new Shower(
                R.string.puppidvelids, GeocentricCoordinates.getInstance(123, -45),
                new GregorianCalendar(ANY_OLD_YEAR, 11, 1),
                new GregorianCalendar(ANY_OLD_YEAR, 11, 7),
                new GregorianCalendar(ANY_OLD_YEAR, 11, 15),
                10));
        showers.add(new Shower(
                R.string.geminids, GeocentricCoordinates.getInstance(112, 33),
                new GregorianCalendar(ANY_OLD_YEAR, 11, 7),
                new GregorianCalendar(ANY_OLD_YEAR, 11, 14),
                new GregorianCalendar(ANY_OLD_YEAR, 11, 17),
                120));
        showers.add(new Shower(
                R.string.ursids, GeocentricCoordinates.getInstance(217, 76),
                new GregorianCalendar(ANY_OLD_YEAR, 11, 17),
                new GregorianCalendar(ANY_OLD_YEAR, 11, 23),
                new GregorianCalendar(ANY_OLD_YEAR, 11, 26),
                10));
    }

    @Override
    protected void initializeAstroSources(List<AstronomicalSource> sources) {
        for (Shower shower : showers) {
            sources.add(new MeteorRadiantSource(model, shower, getResources()));
        }
    }

    @Override
    public int getLayerDepthOrder() {
        return DEPTH_ORDER;
    }

    @Override
    public String getPreferenceId() {
        return PREFERENCE_ID;
    }

    @Override
    protected int getLayerNameId() {
        return R.string.show_meteors_pref;
    }

    /**
     * Represents a meteor shower.
     */
    private static class Shower {

        private final Calendar start;
        private final GeocentricCoordinates radiant;
        private final int nameId;
        private final Calendar peak;
        private final Calendar end;
        private final int peakMeteorsPerHour;

        public Shower(int nameId, GeocentricCoordinates radiant, Calendar start, Calendar peak, Calendar end, int peakMeteorsPerHour) {
            this.nameId = nameId;
            this.radiant = radiant;
            this.start = start;
            this.peak = peak;
            this.end = end;
            this.peakMeteorsPerHour = peakMeteorsPerHour;
        }
    }

    private static class MeteorRadiantSource extends AstronomicalSource {

        private static final int LABEL_COLOR = 0xf67e81;
        private static final long UPDATE_FREQ_MS = TimeUtils.MILLISECONDS_PER_DAY;
        private static final float SCALE_FACTOR = 0.03f;
        private final List<ImageSource> imageSources = Collections.synchronizedList(new ArrayList<>());
        private final List<TextSource> labelSources = Collections.synchronizedList(new ArrayList<>());
        private final List<String> searchNames = Collections.synchronizedList(new ArrayList<>());
        private final AstronomerModel model;
        private final ImageSource theImage;
        private final TextSource label;
        private final Shower shower;
        private final String name;
        private long lastUpdateTimeMs = 0L;

        public MeteorRadiantSource(AstronomerModel model, Shower shower, Resources resources) {
            this.model = model;
            this.shower = shower;
            this.name = resources.getString(shower.nameId);
            // Not sure what the right user experience should be here.  Should we only show up
            // in the search results when the shower is visible?  For now, just ensure
            // that it's obvious from the search label.
            CharSequence startDate = DateFormat.format("MMM dd", shower.start);
            CharSequence endDate = DateFormat.format("MMM dd", shower.end);
            searchNames.add(name + " (" + startDate + "-" + endDate + ")");
            // blank is a 1pxX1px image that should be invisible.
            // We'd prefer not to show any image except on the shower dates, but there
            // appears to be a bug in the renderer/layer interface in that Update values are not
            // respected.  Ditto the label.
            // TODO(johntaylor): fix the bug and remove this blank image
            theImage = new ImageSource(shower.radiant, resources, R.drawable.blank, SCALE_FACTOR);
            imageSources.add(theImage);
            label = new TextSource(shower.radiant, name, LABEL_COLOR);
            labelSources.add(label);
        }

        @Override
        public List<String> getNames() {
            return searchNames;
        }

        @Override
        public GeocentricCoordinates getSearchLocation() {
            return shower.radiant;
        }

        private void updateShower() {
            lastUpdateTimeMs = model.getTimeMillis();
            // We will only show the shower if it's the right time of year.
            Calendar now = model.getTime();
            // Standardize on the same year as we stored for the showers.
            now.set(Calendar.YEAR, ANY_OLD_YEAR);

            theImage.resetUpVector();
            // TODO(johntaylor): consider varying the sizes by scaling factor as time progresses.
            if (now.after(shower.start) && now.before(shower.end)) {
                label.setText(name);
                double percentToPeak;
                if (now.before(shower.peak)) {
                    percentToPeak = (double) (now.getTimeInMillis() - shower.start.getTimeInMillis()) /
                            (shower.peak.getTimeInMillis() - shower.start.getTimeInMillis());
                } else {
                    percentToPeak = (double) (shower.end.getTimeInMillis() - now.getTimeInMillis()) /
                            (shower.end.getTimeInMillis() - shower.peak.getTimeInMillis());
                }
                // Not sure how best to calculate number of meteors - use linear interpolation for now.
                double numberOfMeteorsPerHour = shower.peakMeteorsPerHour * percentToPeak;
                if (numberOfMeteorsPerHour > METEOR_THRESHOLD_PER_HR) {
                    theImage.setImageId(R.drawable.meteors_1);
                } else {
                    theImage.setImageId(R.drawable.meteors_2);
                }
            } else {
                label.setText(" ");
                theImage.setImageId(R.drawable.blank);
            }
        }

        @Override
        public AstronomicalSource initialize() {
            updateShower();
            return this;
        }

        @Override
        public EnumSet<UpdateType> update() {
            EnumSet<UpdateType> updateTypes = EnumSet.noneOf(UpdateType.class);
            if (Math.abs(model.getTime().getTimeInMillis() - lastUpdateTimeMs) > UPDATE_FREQ_MS) {
                updateShower();
                updateTypes.add(UpdateType.Reset);
            }
            return updateTypes;
        }

        @Override
        public List<? extends ImageSource> getImages() {
            return imageSources;
        }

        @Override
        public List<? extends TextSource> getLabels() {
            return labelSources;
        }
    }
}