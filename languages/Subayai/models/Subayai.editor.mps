<?xml version="1.0" encoding="UTF-8"?>
<model ref="r:0b6f5dab-1ede-44ef-8cb6-22cd49cf9467(Subayai.editor)">
  <persistence version="9" />
  <languages>
    <use id="18bc6592-03a6-4e29-a83a-7ff23bde13ba" name="jetbrains.mps.lang.editor" version="15" />
    <use id="aee9cad2-acd4-4608-aef2-0004f6a1cdbd" name="jetbrains.mps.lang.actions" version="4" />
    <devkit ref="fbc25dd2-5da4-483a-8b19-70928e1b62d7(jetbrains.mps.devkit.general-purpose)" />
  </languages>
  <imports>
    <import index="be7m" ref="r:5a89259a-fdd5-49d9-8fe7-a9768b682687(Subayai.structure)" implicit="true" />
  </imports>
  <registry>
    <language id="18bc6592-03a6-4e29-a83a-7ff23bde13ba" name="jetbrains.mps.lang.editor">
      <concept id="1071666914219" name="jetbrains.mps.lang.editor.structure.ConceptEditorDeclaration" flags="ig" index="24kQdi" />
      <concept id="1080736578640" name="jetbrains.mps.lang.editor.structure.BaseEditorComponent" flags="ig" index="2wURMF">
        <child id="1080736633877" name="cellModel" index="2wV5jI" />
      </concept>
      <concept id="1073389577006" name="jetbrains.mps.lang.editor.structure.CellModel_Constant" flags="sn" stub="3610246225209162225" index="3F0ifn">
        <property id="1073389577007" name="text" index="3F0ifm" />
      </concept>
      <concept id="1166049232041" name="jetbrains.mps.lang.editor.structure.AbstractComponent" flags="ng" index="1XWOmA">
        <reference id="1166049300910" name="conceptDeclaration" index="1XX52x" />
      </concept>
    </language>
  </registry>
  <node concept="24kQdi" id="5bGVZ0_gaMq">
    <ref role="1XX52x" to="be7m:5bGVZ0_gaMm" resolve="print" />
    <node concept="3F0ifn" id="5bGVZ0_gaMz" role="2wV5jI">
      <property role="3F0ifm" value="print &lt;space&gt; &lt;property text&gt;" />
    </node>
  </node>
  <node concept="24kQdi" id="1h$GaYG$t56">
    <ref role="1XX52x" to="be7m:1h$GaYG$t52" resolve="StringLiteral" />
    <node concept="3F0ifn" id="1h$GaYG$t58" role="2wV5jI">
      <property role="3F0ifm" value="&quot;\&quot;&quot;&lt;property value&gt;&quot;\&quot;&quot;" />
    </node>
  </node>
  <node concept="24kQdi" id="4upETP$uL_t">
    <ref role="1XX52x" to="be7m:1h$GaYG$t5e" resolve="BoolLiteral" />
    <node concept="3F0ifn" id="4upETP$uLQC" role="2wV5jI">
      <property role="3F0ifm" value="&lt;property value&gt;" />
    </node>
  </node>
  <node concept="24kQdi" id="4upETP$uLQF">
    <ref role="1XX52x" to="be7m:1h$GaYG$t5a" resolve="IntLiteral" />
    <node concept="3F0ifn" id="4upETP$uLQI" role="2wV5jI">
      <property role="3F0ifm" value="&lt;property value&gt;" />
    </node>
  </node>
</model>

