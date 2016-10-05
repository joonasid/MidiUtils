package fi.joonasid.tx81z;

import fi.joonasid.midiutil.MidiUtils;

import java.io.PrintStream;

/**
 * @author <a href="mailto:joonas.id@iki.fi">Joonas Id</a>
 */
public class Main {

    private static final String CH_MASK = "0001nnnn";
    private static final String GROUP_MASK = "0ggggghh";
    private static final String PARAM_NO_MASK = "0ppppppp";
    private static final String DATA_MASK = "0vvvvvvv";

    static class ParamEmitter {

        private static final int ID_BYTE = 0;
        private static final int CH_BYTE = 1;
        private static final int GROUP_BYTE = 2;
        private static final int PARAM_BYTE = 3;
        private static final int DATA_BYTE = 4;

        private final PrintStream out;
        private int paramNo;

        /*
        * 0 43 ID
        * 1 1ccccccc c = channel
        * 2 0ggggghh g = group ID, h = subgroup ID
        * 3 0ppppppp p = param ID
        * 4 0ddddddd d = data
        */
        private String[] bytes;

        ParamEmitter(PrintStream out, String dataMask) {
            this.bytes = new String[DATA_BYTE + 1];
            this.out = out;
            this.bytes[DATA_BYTE] = dataMask;
        }

        ParamEmitter setDeviceId(int deviceId) {
            this.bytes[ID_BYTE] = MidiUtils.getHexByte(deviceId);
            return this;
        }

        ParamEmitter setChannel(int channel) {
            this.bytes[CH_BYTE] = MidiUtils.getHexByte(CH_MASK, channel);
            return this;
        }

        ParamEmitter beginParamGroup(int paramGroup, int subgroup, int firstParamNo, String name) {
            this.bytes[GROUP_BYTE] = MidiUtils.getHexByte(GROUP_MASK, paramGroup, subgroup);
            this.paramNo = firstParamNo;

            this.out.printf("\nParameter group '%1$s':\n", name);
            return this;
        }

        ParamEmitter emit(String paramName) {
            String paramByte = MidiUtils.getHexByte(PARAM_NO_MASK, this.paramNo);

            this.out.printf("\"%6$d\";\"%7$s\";\"%1$s %2$s %3$s %4$s %5$s\"\n",
                    this.bytes[ID_BYTE],
                    this.bytes[CH_BYTE],
                    this.bytes[GROUP_BYTE],
                    paramByte,
                    this.bytes[DATA_BYTE],
                    this.paramNo,
                    paramName
            );

            this.paramNo++;
            return this;
        }
    }

    public static void main(String... args) {
        ParamEmitter e = new ParamEmitter(System.out, "VV");
        e.setChannel(1);
        e.setDeviceId(Integer.parseInt("43", 16));

        e.beginParamGroup(4, 2, 0, "Voice Edit");
        for (int op = 1; op <= 4; op++) {
            e.emit("OP" + op + " Attack Rate");
            e.emit("OP" + op + " Decay 1 Rate");
            e.emit("OP" + op + " Decay 2 Rate");
            e.emit("OP" + op + " Release Rate");
            e.emit("OP" + op + " Decay 1 Level");
            e.emit("OP" + op + " Level Scaling");
            e.emit("OP" + op + " Rate Scaling");
            e.emit("OP" + op + " EG Bias Sensitivity");
            e.emit("OP" + op + " Amp Mod Enable");
            e.emit("OP" + op + " Key Vel Sensitivity");
            e.emit("OP" + op + " Output Level");
            e.emit("OP" + op + " Frequency");
            e.emit("OP" + op + " Detune");
        }
        e.emit("Algorithm");
        e.emit("Feedback");
        e.emit("LFO Speed");
        e.emit("LFO Delay");
        e.emit("LFO Pitch Mod Depth");
        e.emit("LFO Amp Mod Depth");
        e.emit("LFO Sync");
        e.emit("LFO Wave");
        e.emit("Pitch Mod Sens");
        e.emit("Amp Mod Sens");
        e.emit("Transpose");
        e.emit("Poly / Mono");
        e.emit("Pitch Bend Range");
        e.emit("Portamento");
        e.emit("Portamento Time");
        e.emit("Foot Control Volume");
        e.emit("Sustain");
        e.emit("Portamento");
        e.emit("Chorus");
        e.emit("Mod Wheel Pitch");
        e.emit("Mod Wheel Amp");
        e.emit("Breath Ctrl Pitch");
        e.emit("Breath Ctrl Amp");
        e.emit("Breath Ctrl Pitch Bias");
        e.emit("Breath Ctrl EG Bias");
        e.emit("Voice Name char1");
        e.emit("Voice Name char2");
        e.emit("Voice Name char3");
        e.emit("Voice Name char4");
        e.emit("Voice Name char5");
        e.emit("Voice Name char6");
        e.emit("Voice Name char7");
        e.emit("Voice Name char9");
        e.emit("Voice Name char10");

        e.beginParamGroup(4, 2, 93, "Operator on/off");
        e.emit("OP 1-4 on/off");

        e.beginParamGroup(4, 3, 0, "Voice Edit Additional Parameters");
        for (int op = 1; op <= 4; op++) {
            e.emit("OP" + op + " Fixed Frequency");
            e.emit("OP" + op + " Fix Frequency Range");
            e.emit("OP" + op + " Frequency Range Fine");
            e.emit("OP" + op + " Waveform");
            e.emit("OP" + op + " EG Shift");
        }
        e.emit("Reverb Rate");
        e.emit("Foot Controller Pitch");
        e.emit("Foot Controller Amp");
    }
}
