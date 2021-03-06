package io.github.marcocipriani01.telescopetouch.data;

import java.io.IOException;
import java.util.List;

import io.github.marcocipriani01.telescopetouch.source.proto.SourceProto;


/**
 * Class for reading the messier csv file and writing the contents to protocol
 * buffers.
 *
 * @author brent@google.com (Brent Bryan)
 */
public class MessierAsciiProtoWriter extends AbstractAsciiProtoWriter {
    // TODO(mrhector): verify colors
    private static final int LABEL_COLOR = 0x48a841; // argb
    private static final int POINT_COLOR = 0x48a841; // abgr (!)
    private static final int POINT_SIZE = 3;

    public static void main(String[] args) throws IOException {
        new MessierAsciiProtoWriter().run(args);
    }

    @Override
    protected SourceProto.AstronomicalSourceProto getSourceFromLine(String line, int index) {
        // name, type, RA(h), dec(degrees), magnitude, size, ngc, constellation,
        // names, common name
        // Of these, only name(0), ra(2), & dec(3) are used.
        if (line.startsWith("Object,Type")) {
            return null;
        }

        // TODO(brent): Add image shapes here?

        String[] tokens = line.split(",");

        // Convert from hours to degrees.
        float ra = 15 * Float.parseFloat(tokens[2]);
        float dec = Float.parseFloat(tokens[3]);
        float magnitude = 4.9f;

        SourceProto.AstronomicalSourceProto.Builder sourceBuilder = SourceProto.AstronomicalSourceProto.newBuilder();
        SourceProto.GeocentricCoordinatesProto coords = getCoords(ra, dec);

        SourceProto.LabelElementProto.Builder labelBuilder = SourceProto.LabelElementProto.newBuilder();
        labelBuilder.setColor(LABEL_COLOR);
        labelBuilder.setLocation(coords);
        List<String> rKeysForName = rKeysFromName(tokens[0]);
        if (!rKeysForName.isEmpty()) {
            labelBuilder.setStringsStrId(rKeysForName.get(0));
        }
        sourceBuilder.addLabel(labelBuilder);

        SourceProto.PointElementProto.Builder pointBuilder = SourceProto.PointElementProto.newBuilder();
        pointBuilder.setColor(POINT_COLOR);
        pointBuilder.setLocation(coords);
        pointBuilder.setSize(POINT_SIZE);
        // TODO(johntaylor): consider setting messier object shape
        sourceBuilder.addPoint(pointBuilder);
        for (String rKey : rKeysForName) {
            sourceBuilder.addNameStrIds(rKey);
        }

        sourceBuilder.setSearchLocation(coords);
        return sourceBuilder.build();
    }
}