package unluac.parse;

import java.nio.ByteBuffer;

public class LFunction extends BObject {

    public BHeader header;
    public LFunction parent;
    public long[] code;  // 修改为 long[] 以适应 8 字节指令
    public LLocal[] locals;
    public LObject[] constants;
    public LUpvalue[] upvalues;
    public LFunction[] functions;
    public int maximumStackSize;
    public int numUpvalues;
    public int numParams;
    public int vararg;
    public boolean stripped;

    // 构造函数需要对 code 参数类型进行修改
    public LFunction(BHeader header, ByteBuffer buffer) {
        this.header = header;
        this.parent = null;  // 通常由调用者设置
        this.maximumStackSize = buffer.getInt();
        this.numUpvalues = buffer.getInt();
        this.numParams = buffer.getInt();
        this.vararg = buffer.getInt();
        this.stripped = buffer.get() != 0;

        this.code = parseCode(buffer);
        this.locals = parseLocals(buffer);
        this.constants = parseConstants(buffer);
        this.upvalues = parseUpvalues(buffer);
        this.functions = parseFunctions(buffer);
    }

    private long[] parseCode(ByteBuffer buffer) {
        int length = buffer.getInt();
        long[] codes = new long[length];
        for (int i = 0; i < length; i++) {
            codes[i] = (header.instructionSize == 8) ? buffer.getLong() : buffer.getInt() & 0xFFFFFFFFL;
        }
        return codes;
    }

    private LLocal[] parseLocals(ByteBuffer buffer) {
        int count = buffer.getInt();
        LLocal[] locals = new LLocal[count];
        for (int i = 0; i < count; i++) {
            String name = StringUtil.readLuaString(buffer);
            int start = buffer.getInt();
            int end = buffer.getInt();
            locals[i] = new LLocal(name, start, end);
        }
        return locals;
    }

    private LObject[] parseConstants(ByteBuffer buffer) {
        int count = buffer.getInt();
        LObject[] constants = new LObject[count];
        for (int i = 0; i < count; i++) {
            constants[i] = LObject.readObject(buffer);
        }
        return constants;
    }

    private LUpvalue[] parseUpvalues(ByteBuffer buffer) {
        int count = buffer.getInt();
        LUpvalue[] upvalues = new LUpvalue[count];
        for (int i = 0; i < count; i++) {
            String name = StringUtil.readLuaString(buffer);
            upvalues[i] = new LUpvalue(name);
        }
        return upvalues;
    }

    private LFunction[] parseFunctions(ByteBuffer buffer) {
        int count = buffer.getInt();
        LFunction[] functions = new LFunction[count];
        for (int i = 0; i < count; i++) {
            functions[i] = new LFunction(header, buffer);
        }
        return functions;
    }
}
