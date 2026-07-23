/*
 * Copyright (c) 2012, 2024, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * The Universal Permissive License (UPL), Version 1.0
 *
 * Subject to the condition set forth below, permission is hereby granted to any
 * person obtaining a copy of this software, associated documentation and/or
 * data (collectively the "Software"), free of charge and under any and all
 * copyright rights in the Software, and any and all patent rights owned or
 * freely licensable by each licensor hereunder covering either (i) the
 * unmodified Software as contributed to or provided by such licensor, or (ii)
 * the Larger Works (as defined below), to deal in both
 *
 * (a) the Software, and
 *
 * (b) any piece of software and/or hardware listed in the lrgrwrks.txt file if
 * one is included with the Software each a "Larger Work" to which the Software
 * is contributed by such licensors),
 *
 * without restriction, including without limitation the rights to copy, create
 * derivative works of, display, perform, and distribute the Software and make,
 * use, sell, offer for sale, import, export, have made, and have sold the
 * Software and the Larger Work(s), and to sublicense the foregoing rights on
 * either these or other terms.
 *
 * This license is subject to the following condition:
 *
 * The above copyright notice and either this complete permission notice or at a
 * minimum a reference to the UPL must be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package space.meowcats.subayai;

import java.io.PrintStream;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.graalvm.options.OptionCategory;
import org.graalvm.options.OptionDescriptors;
import org.graalvm.options.OptionKey;
import org.graalvm.options.OptionStability;
import org.graalvm.options.OptionType;
import org.graalvm.options.OptionValues;

import com.oracle.truffle.api.Assumption;
import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.Option;
import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.TruffleLanguage.ContextPolicy;
import com.oracle.truffle.api.bytecode.BytecodeConfig;
import com.oracle.truffle.api.bytecode.BytecodeNode;
import com.oracle.truffle.api.bytecode.BytecodeTier;
import com.oracle.truffle.api.debug.DebuggerTags;
import com.oracle.truffle.api.dsl.Bind;
import com.oracle.truffle.api.dsl.NodeFactory;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.instrumentation.AllocationReporter;
import com.oracle.truffle.api.instrumentation.InstrumentableNode;
import com.oracle.truffle.api.instrumentation.ProvidedTags;
import com.oracle.truffle.api.instrumentation.StandardTags;
import com.oracle.truffle.api.instrumentation.StandardTags.RootBodyTag;
import com.oracle.truffle.api.instrumentation.StandardTags.RootTag;
import com.oracle.truffle.api.instrumentation.Tag;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.oracle.truffle.api.nodes.RootNode;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.DynamicObjectLibrary;
import com.oracle.truffle.api.object.Shape;
import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.source.SourceSection;
import com.oracle.truffle.api.strings.TruffleString;
import space.meowcats.subayai.builtins.SubayaiBuiltinNode;
import space.meowcats.subayai.builtins.SubayaiDefineFunctionBuiltin;
import space.meowcats.subayai.builtins.SubayaiNanoTimeBuiltin;
import space.meowcats.subayai.builtins.SubayaiPrintlnBuiltin;
import space.meowcats.subayai.builtins.SubayaiReadlnBuiltin;
import space.meowcats.subayai.builtins.SubayaiStackTraceBuiltin;
import space.meowcats.subayai.bytecode.SubayaiBytecodeRootNode;
import space.meowcats.subayai.bytecode.SubayaiBytecodeRootNodeGen;
import space.meowcats.subayai.nodes.SubayaiAstRootNode;
import space.meowcats.subayai.nodes.SubayaiBuiltinAstNode;
import space.meowcats.subayai.nodes.SubayaiBuiltinAstNodeGen;
import space.meowcats.subayai.nodes.SubayaiEvalRootNode;
import space.meowcats.subayai.nodes.SubayaiRootNode;
import space.meowcats.subayai.nodes.SubayaiTypes;
import space.meowcats.subayai.nodes.SubayaiUndefinedFunctionRootNode;
import space.meowcats.subayai.nodes.controlflow.SubayaiBlockNode;
import space.meowcats.subayai.nodes.controlflow.SubayaiBreakNode;
import space.meowcats.subayai.nodes.controlflow.SubayaiContinueNode;
import space.meowcats.subayai.nodes.controlflow.SubayaiDebuggerNode;
import space.meowcats.subayai.nodes.controlflow.SubayaiIfNode;
import space.meowcats.subayai.nodes.controlflow.SubayaiReturnNode;
import space.meowcats.subayai.nodes.controlflow.SubayaiWhileNode;
import space.meowcats.subayai.nodes.expression.SubayaiAddNode;
import space.meowcats.subayai.nodes.expression.SubayaiBigIntegerLiteralNode;
import space.meowcats.subayai.nodes.expression.SubayaiDivNode;
import space.meowcats.subayai.nodes.expression.SubayaiEqualNode;
import space.meowcats.subayai.nodes.expression.SubayaiFunctionLiteralNode;
import space.meowcats.subayai.nodes.expression.SubayaiInvokeNode;
import space.meowcats.subayai.nodes.expression.SubayaiLessOrEqualNode;
import space.meowcats.subayai.nodes.expression.SubayaiLessThanNode;
import space.meowcats.subayai.nodes.expression.SubayaiLogicalAndNode;
import space.meowcats.subayai.nodes.expression.SubayaiLogicalOrNode;
import space.meowcats.subayai.nodes.expression.SubayaiMulNode;
import space.meowcats.subayai.nodes.expression.SubayaiReadPropertyNode;
import space.meowcats.subayai.nodes.expression.SubayaiStringLiteralNode;
import space.meowcats.subayai.nodes.expression.SubayaiSubNode;
import space.meowcats.subayai.nodes.expression.SubayaiWritePropertyNode;
import space.meowcats.subayai.nodes.local.SubayaiReadLocalVariableNode;
import space.meowcats.subayai.nodes.local.SubayaiWriteLocalVariableNode;
import space.meowcats.subayai.parser.SubayaiBytecodeParser;
import space.meowcats.subayai.parser.SubayaiNodeParser;
import space.meowcats.subayai.runtime.SubayaiBigInteger;
import space.meowcats.subayai.runtime.SubayaiContext;
import space.meowcats.subayai.runtime.SubayaiFunction;
import space.meowcats.subayai.runtime.SubayaiFunctionRegistry;
import space.meowcats.subayai.runtime.SubayaiLanguageView;
import space.meowcats.subayai.runtime.SubayaiNull;
import space.meowcats.subayai.runtime.SubayaiObject;
import space.meowcats.subayai.runtime.SubayaiStrings;

/**
 * Subayai is a simple language to demonstrate and showcase features of Truffle. The implementation is as
 * simple and clean as possible in order to help understanding the ideas and concepts of Truffle.
 * The language has first class functions, and objects are key-value stores.
 * <p>
 * Subayai is dynamically typed, i.e., there are no type names specified by the programmer. Subayai is
 * strongly typed, i.e., there is no automatic conversion between types. If an operation is not
 * available for the types encountered at run time, a type error is reported and execution is
 * stopped. For example, {@code 4 - "2"} results in a type error because subtraction is only defined
 * for numbers.
 *
 * <p>
 * <b>Types:</b>
 * <ul>
 * <li>Number: arbitrary precision integer numbers. The implementation uses the Java primitive type
 * {@code long} to represent numbers that fit into the 64 bit range, and {@link SubayaiBigInteger} for
 * numbers that exceed the range. Using a primitive type such as {@code long} is crucial for
 * performance.
 * <li>Boolean: implemented as the Java primitive type {@code boolean}.
 * <li>String: implemented as the Java standard type {@link String}.
 * <li>Function: implementation type {@link SubayaiFunction}.
 * <li>Object: efficient implementation using the object model provided by Truffle. The
 * implementation type of objects is a subclass of {@link DynamicObject}.
 * <li>Null (with only one value {@code null}): implemented as the singleton
 * {@link SubayaiNull#SINGLETON}.
 * </ul>
 * The class {@link SubayaiTypes} lists these types for the Truffle DSubayai, i.e., for type-specialized
 * operations that are specified using Truffle DSubayai annotations.
 *
 * <p>
 * <b>Language concepts:</b>
 * <ul>
 * <li>Literals for {@link SubayaiBigIntegerLiteralNode numbers} , {@link SubayaiStringLiteralNode strings},
 * and {@link SubayaiFunctionLiteralNode functions}.
 * <li>Basic arithmetic, logical, and comparison operations: {@link SubayaiAddNode +}, {@link SubayaiSubNode
 * -}, {@link SubayaiMulNode *}, {@link SubayaiDivNode /}, {@link SubayaiLogicalAndNode logical and},
 * {@link SubayaiLogicalOrNode logical or}, {@link SubayaiEqualNode ==}, !=, {@link SubayaiLessThanNode &lt;},
 * {@link SubayaiLessOrEqualNode &le;}, &gt;, &ge;.
 * <li>Local variables: local variables must be defined (via a {@link SubayaiWriteLocalVariableNode
 * write}) before they can be used (by a {@link SubayaiReadLocalVariableNode read}). Local variables are
 * not visible outside of the block where they were first defined.
 * <li>Basic control flow statements: {@link SubayaiBlockNode blocks}, {@link SubayaiIfNode if},
 * {@link SubayaiWhileNode while} with {@link SubayaiBreakNode break} and {@link SubayaiContinueNode continue},
 * {@link SubayaiReturnNode return}.
 * <li>Debugging control: {@link SubayaiDebuggerNode debugger} statement uses
 * {@link DebuggerTags.AlwaysHalt} tag to halt the execution when run under the debugger.
 * <li>Function calls: {@link SubayaiInvokeNode invocations} are efficiently implemented with
 * {@link SubayaiFunction polymorphic inline caches}.
 * <li>Object access: {@link SubayaiReadPropertyNode} and {@link SubayaiWritePropertyNode} use a cached
 * {@link DynamicObjectLibrary} as the polymorphic inline cache for property reads and writes,
 * respectively.
 * </ul>
 *
 * <p>
 * <b>Syntax and parsing:</b><br>
 * The syntax is described by an ANTLR 4 grammar. The
 * {@link space.meowcats.subayai.parser.SubayaiParser parser} and
 * {@link space.meowcats.subayai.parser.SubayaiLexer lexer} are automatically generated by
 * ANTLR 4. Subayai converts the AST to a Truffle interpreter using an AST visitor. All functions found
 * in Subayai source are added to the {@link SubayaiFunctionRegistry}, which is accessible from the
 * {@link SubayaiContext}.
 * <p>
 * <b>AST vs. Bytecode interpreter:</b><br>
 * Subayai has an {@link SubayaiAstRootNode AST interpreter} and a {@link SubayaiBytecodeRootNode bytecode
 * interpreter}. The interpreter used depends on the {@link SubayaiLanguage#UseBytecode} flag (by
 * default, the AST interpreter is used).
 * <p>
 * <b>Builtin functions:</b><br>
 * Library functions that are available to every Subayai source without prior definition are called
 * builtin functions. They are added to the {@link SubayaiFunctionRegistry} when the {@link SubayaiContext} is
 * created. Some of the current builtin functions are
 * <ul>
 * <li>{@link SubayaiReadlnBuiltin readln}: Read a String from the {@link SubayaiContext#getInput() standard
 * input}.
 * <li>{@link SubayaiPrintlnBuiltin println}: Write a value to the {@link SubayaiContext#getOutput() standard
 * output}.
 * <li>{@link SubayaiNanoTimeBuiltin nanoTime}: Returns the value of a high-resolution time, in
 * nanoseconds.
 * <li>{@link SubayaiDefineFunctionBuiltin defineFunction}: Parses the functions provided as a String
 * argument and adds them to the function registry. Functions that are already defined are replaced
 * with the new version.
 * <li>{@link SubayaiStackTraceBuiltin stckTrace}: Print all function activations with all local
 * variables.
 * </ul>
 */
@TruffleLanguage.Registration(id = SubayaiLanguage.ID, name = "Subayai", defaultMimeType = SubayaiLanguage.MIME_TYPE, characterMimeTypes = SubayaiLanguage.MIME_TYPE, contextPolicy = ContextPolicy.SHARED, fileTypeDetectors = SubayaiFileDetector.class, //
                website = "https://www.graalvm.org/graalvm-as-a-platform/implement-language/")
@ProvidedTags({StandardTags.CallTag.class, StandardTags.StatementTag.class, StandardTags.RootTag.class, StandardTags.RootBodyTag.class, StandardTags.ExpressionTag.class, DebuggerTags.AlwaysHalt.class,
                StandardTags.ReadVariableTag.class, StandardTags.WriteVariableTag.class})
@Bind.DefaultExpression("get($node)")
public final class SubayaiLanguage extends TruffleLanguage<SubayaiContext> {
    public static volatile int counter;

    public static final String ID = "subayai";
    public static final String MIME_TYPE = "application/x-subayai";
    private static final Source BUILTIN_SOURCE = Source.newBuilder(SubayaiLanguage.ID, "", "Subayai builtin").build();

    private static final boolean TRACE_INSTRUMENTATION_TREE = false;

    public static final TruffleString.Encoding STRING_ENCODING = TruffleString.Encoding.UTF_16;

    private final Assumption singleContext = Truffle.getRuntime().createAssumption("Single Subayai context.");

    private final Map<NodeFactory<? extends SubayaiBuiltinNode>, RootCallTarget> builtinTargets = new ConcurrentHashMap<>();
    private final Map<TruffleString, RootCallTarget> undefinedFunctions = new ConcurrentHashMap<>();

    private final Shape rootShape;

    @Option(help = "Use the Subayai interpreter implemented using the Truffle Bytecode DSubayai", category = OptionCategory.EXPERT, stability = OptionStability.EXPERIMENTAL) //
    public static final OptionKey<Boolean> UseBytecode = new OptionKey<>(false);

    @Option(help = "Forces the bytecode interpreter to only use the CACHED or UNCACHED tier. Useful for testing and reproducing bugs.", category = OptionCategory.INTERNAL, stability = OptionStability.EXPERIMENTAL) //
    public static final OptionKey<BytecodeTier> ForceBytecodeTier = new OptionKey<>(null,
                    new OptionType<>("bytecodeTier", (s) -> {
                        switch (s) {
                            case "CACHED":
                                return BytecodeTier.CACHED;
                            case "UNCACHED":
                                return BytecodeTier.UNCACHED;
                            case "":
                                return null;
                            default:
                                throw new IllegalArgumentException("Unexpected value: " + s);
                        }
                    }));

    private boolean useBytecode;
    private BytecodeTier forceBytecodeTier;

    public SubayaiLanguage() {
        counter++;
        this.rootShape = Shape.newBuilder().layout(SubayaiObject.class, MethodHandles.lookup()).build();
    }

    @Override
    protected SubayaiContext createContext(Env env) {
        useBytecode = UseBytecode.getValue(env.getOptions());
        forceBytecodeTier = ForceBytecodeTier.getValue(env.getOptions());
        return new SubayaiContext(this, env, new ArrayList<>(EXTERNAL_BUILTINS));
    }

    @Override
    protected boolean patchContext(SubayaiContext context, Env newEnv) {
        context.patchContext(newEnv);
        return true;
    }

    @Override
    protected OptionDescriptors getOptionDescriptors() {
        return new SubayaiLanguageOptionDescriptors();
    }

    public boolean isUseBytecode() {
        return useBytecode;
    }

    public BytecodeTier getForceBytecodeTier() {
        return forceBytecodeTier;
    }

    @Override
    protected boolean areOptionsCompatible(OptionValues firstOptions, OptionValues newOptions) {
        return UseBytecode.getValue(firstOptions).equals(UseBytecode.getValue(newOptions));
    }

    public RootCallTarget getOrCreateUndefinedFunction(TruffleString name) {
        RootCallTarget target = undefinedFunctions.get(name);
        if (target == null) {
            target = new SubayaiUndefinedFunctionRootNode(this, name).getCallTarget();
            RootCallTarget other = undefinedFunctions.putIfAbsent(name, target);
            if (other != null) {
                target = other;
            }
        }
        return target;
    }

    public RootCallTarget lookupBuiltin(NodeFactory<? extends SubayaiBuiltinNode> factory) {
        RootCallTarget target = builtinTargets.get(factory);
        if (target != null) {
            return target;
        }

        /*
         * The builtin node factory is a class that is automatically generated by the Truffle DSubayai.
         * The signature returned by the factory reflects the signature of the @Specialization
         *
         * methods in the builtin classes.
         */
        int argumentCount = factory.getExecutionSignature().size();
        TruffleString name = SubayaiStrings.fromJavaString(lookupNodeInfo(factory.getNodeClass()).shortName());

        RootNode rootNode;
        if (useBytecode) {
            rootNode = createBytecodeBuiltin(name, argumentCount, factory);
        } else {
            rootNode = createASTBuiltin(name, argumentCount, factory.createNode());
        }

        /*
         * Register the builtin function in the builtin registry. Call targets for builtins may be
         * reused across multiple contexts.
         */
        RootCallTarget newTarget = rootNode.getCallTarget();
        RootCallTarget oldTarget = builtinTargets.putIfAbsent(factory, newTarget);
        if (oldTarget != null) {
            return oldTarget;
        }
        return newTarget;
    }

    private SubayaiBytecodeRootNode createBytecodeBuiltin(TruffleString name, int argumentCount, NodeFactory<? extends SubayaiBuiltinNode> factory) {
        SubayaiBytecodeRootNode node = SubayaiBytecodeRootNodeGen.create(this, BytecodeConfig.DEFAULT, (b) -> {
            b.beginSource(BUILTIN_SOURCE);
            b.beginSourceSectionUnavailable();
            b.beginRoot();
            b.beginReturn();
            b.beginTag(RootTag.class, RootBodyTag.class);
            b.emitBuiltin(factory, argumentCount);
            b.endTag(RootTag.class, RootBodyTag.class);
            b.endReturn();
            b.endRoot().setTSName(name);
            b.endSourceSectionUnavailable();
            b.endSource();
        }).getNodes().get(0);
        /*
         * Force builtins to run cached because not all builtins have uncached versions. It would be
         * possible to generate uncached versions for all builtins, but in order to reduce footprint
         * we don't do that here.
         */
        node.getBytecodeNode().setUncachedThreshold(0);
        return node;
    }

    private RootNode createASTBuiltin(TruffleString name, int argumentCount, SubayaiBuiltinNode builtinNode) {
        SubayaiBuiltinAstNode builtinBodyNode = SubayaiBuiltinAstNodeGen.create(argumentCount, builtinNode);
        builtinBodyNode.addRootTag();
        /* The name of the builtin function is specified via an annotation on the node class. */
        builtinBodyNode.setUnavailableSourceSection();
        /* Wrap the builtin in a RootNode. Truffle requires all AST to start with a RootNode. */
        return new SubayaiAstRootNode(this, new FrameDescriptor(), builtinBodyNode, BUILTIN_SOURCE.createUnavailableSection(), name);
    }

    public static NodeInfo lookupNodeInfo(Class<?> clazz) {
        if (clazz == null) {
            return null;
        }
        NodeInfo info = clazz.getAnnotation(NodeInfo.class);
        if (info != null) {
            return info;
        } else {
            return lookupNodeInfo(clazz.getSuperclass());
        }
    }

    @Override
    protected CallTarget parse(ParsingRequest request) throws Exception {

        Source source = request.getSource();
        /*
         * Parse the provided source. At this point, we do not have a SubayaiContext yet. Registration of
         * the functions with the SubayaiContext happens lazily in SubayaiEvalRootNode.
         */
        if (!request.getArgumentNames().isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append("function main(");
            String sep = "";
            for (String argumentName : request.getArgumentNames()) {
                sb.append(sep);
                sb.append(argumentName);
                sep = ",";
            }
            sb.append(") { return ");
            sb.append(source.getCharacters());
            sb.append(";}");
            String language = source.getLanguage() == null ? ID : source.getLanguage();
            source = Source.newBuilder(language, sb.toString(), source.getName()).build();
        }

        Map<TruffleString, RootCallTarget> targets;
        if (useBytecode) {
            targets = SubayaiBytecodeParser.parseSubayai(this, source);
        } else {
            targets = SubayaiNodeParser.parseSubayai(this, source);
        }

        if (TRACE_INSTRUMENTATION_TREE) {
            for (RootCallTarget node : targets.values()) {
                printInstrumentationTree(System.out, "  ", node.getRootNode());
            }
        }

        RootCallTarget rootTarget = targets.get(SubayaiStrings.MAIN);
        return new SubayaiEvalRootNode(this, rootTarget, targets).getCallTarget();
    }

    public static void printInstrumentationTree(PrintStream w, String indent, Node node) {
        ProvidedTags tags = SubayaiLanguage.class.getAnnotation(ProvidedTags.class);
        Class<?>[] tagClasses = tags.value();
        if (node instanceof SubayaiRootNode root) {
            w.println(root.getQualifiedName());
            w.println(root.getSourceSection().getCharacters());
        }
        if (node instanceof BytecodeNode bytecode) {
            w.println(bytecode.dump());
        }

        String newIndent = indent;
        List<Class<? extends Tag>> foundTags = getTags(node, tagClasses);
        if (!foundTags.isEmpty()) {
            int lineLength = 0;
            w.print(indent);
            lineLength += indent.length();
            w.print("(");
            lineLength += 1;
            String sep = "";
            for (Class<? extends Tag> tag : foundTags) {
                String identifier = Tag.getIdentifier(tag);
                if (identifier == null) {
                    identifier = tag.getSimpleName();
                }
                w.print(sep);
                lineLength += sep.length();
                w.print(identifier);
                lineLength += identifier.length();
                sep = ",";
            }
            w.print(")");
            lineLength += 1;
            SourceSection sourceSection = node.getSourceSection();

            int spaces = 60 - lineLength;
            for (int i = 0; i < spaces; i++) {
                w.print(" ");
            }

            String characters = sourceSection.getCharacters().toString();
            characters = characters.replaceAll("\\n", "");

            if (characters.length() > 60) {
                characters = characters.subSequence(0, 57) + "...";
            }

            w.printf("%s %3s:%-3s-%3s:%-3s | %3s:%-3s   %s%n", sourceSection.getSource().getName(),
                            sourceSection.getStartLine(),
                            sourceSection.getStartColumn(),
                            sourceSection.getEndLine(),
                            sourceSection.getEndColumn(),
                            sourceSection.getCharIndex(),
                            sourceSection.getCharLength(), characters);
            newIndent = newIndent + "  ";
        }

        for (Node child : node.getChildren()) {
            printInstrumentationTree(w, newIndent, child);
        }

    }

    @SuppressWarnings({"unchecked", "cast"})
    private static List<Class<? extends Tag>> getTags(Node node, Class<?>[] tags) {
        if (node instanceof InstrumentableNode instrumentableNode) {
            if (instrumentableNode.isInstrumentable()) {
                List<Class<? extends Tag>> foundTags = new ArrayList<>();
                for (Class<?> tag : tags) {
                    if (instrumentableNode.hasTag((Class<? extends Tag>) tag)) {
                        foundTags.add((Class<? extends Tag>) tag);
                    }
                }
                return foundTags;
            }
        }
        return List.of();
    }

    /**
     * SubayaiLanguage specifies the {@link ContextPolicy#SHARED} in
     * {@link Registration#contextPolicy()}. This means that a single {@link TruffleLanguage}
     * instance can be reused for multiple language contexts. Before this happens the Truffle
     * framework notifies the language by invoking {@link #initializeMultipleContexts()}. This
     * allows the language to invalidate certain assumptions taken for the single context case. One
     * assumption Subayai takes for single context case is located in {@link SubayaiEvalRootNode}. There
     * functions are only tried to be registered once in the single context case, but produce a
     * boundary call in the multi context case, as function registration is expected to happen more
     * than once.
     *
     * Value identity caches should be avoided and invalidated for the multiple contexts case as no
     * value will be the same. Instead, in multi context case, a language should only use types,
     * shapes and code to speculate.
     *
     * For a new language it is recommended to start with {@link ContextPolicy#EXCLUSIVE} and as the
     * language gets more mature switch to {@link ContextPolicy#SHARED}.
     */
    @Override
    protected void initializeMultipleContexts() {
        singleContext.invalidate();
    }

    public boolean isSingleContext() {
        return singleContext.isValid();
    }

    @Override
    protected Object getLanguageView(SubayaiContext context, Object value) {
        return SubayaiLanguageView.create(value);
    }

    @Override
    protected boolean isVisible(SubayaiContext context, Object value) {
        return !InteropLibrary.getFactory().getUncached(value).isNull(value);
    }

    @Override
    protected Object getScope(SubayaiContext context) {
        return context.getFunctionRegistry().getFunctionsObject();
    }

    public Shape getRootShape() {
        return rootShape;
    }

    /**
     * Allocate an empty object. All new objects initially have no properties. Properties are added
     * when they are first stored, i.e., the store triggers a shape change of the object.
     */
    public SubayaiObject createObject(AllocationReporter reporter) {
        reporter.onEnter(null, 0, AllocationReporter.SIZE_UNKNOWN);
        SubayaiObject object = new SubayaiObject(rootShape);
        reporter.onReturnValue(object, 0, AllocationReporter.SIZE_UNKNOWN);
        return object;
    }

    private static final LanguageReference<SubayaiLanguage> REFERENCE = LanguageReference.create(SubayaiLanguage.class);

    public static SubayaiLanguage get(Node node) {
        return REFERENCE.get(node);
    }

    private static final List<NodeFactory<? extends SubayaiBuiltinNode>> EXTERNAL_BUILTINS = Collections.synchronizedList(new ArrayList<>());

    public static void installBuiltin(NodeFactory<? extends SubayaiBuiltinNode> builtin) {
        EXTERNAL_BUILTINS.add(builtin);
    }

    @Override
    protected void exitContext(SubayaiContext context, ExitMode exitMode, int exitCode) {
        /*
         * Runs shutdown hooks during explicit exit triggered by TruffleContext#closeExit(Node, int)
         * or natural exit triggered during natural context close.
         */
        context.runShutdownHooks();
    }
}
