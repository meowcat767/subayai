<?xml version="1.0" encoding="UTF-8"?>
<model ref="r:5a89259a-fdd5-49d9-8fe7-a9768b682687(Subayai.structure)">
  <persistence version="9" />
  <languages>
    <devkit ref="78434eb8-b0e5-444b-850d-e7c4ad2da9ab(jetbrains.mps.devkit.aspect.structure)" />
  </languages>
  <imports />
  <registry>
    <language id="c72da2b9-7cce-4447-8389-f407dc1158b7" name="jetbrains.mps.lang.structure">
      <concept id="1169125787135" name="jetbrains.mps.lang.structure.structure.AbstractConceptDeclaration" flags="ig" index="PkWjJ">
        <property id="6714410169261853888" name="conceptId" index="EcuMT" />
        <property id="4628067390765907488" name="conceptShortDescription" index="R4oN_" />
        <child id="1071489727083" name="linkDeclaration" index="1TKVEi" />
      </concept>
      <concept id="1071489090640" name="jetbrains.mps.lang.structure.structure.ConceptDeclaration" flags="ig" index="1TIwiD">
        <reference id="1071489389519" name="extends" index="1TJDcQ" />
      </concept>
      <concept id="1071489288298" name="jetbrains.mps.lang.structure.structure.LinkDeclaration" flags="ig" index="1TJgyj">
        <property id="1071599776563" name="role" index="20kJfa" />
        <property id="1071599893252" name="sourceCardinality" index="20lbJX" />
        <property id="1071599937831" name="metaClass" index="20lmBu" />
        <property id="241647608299431140" name="linkId" index="IQ2ns" />
        <reference id="1071599976176" name="target" index="20lvS9" />
      </concept>
    </language>
    <language id="ceab5195-25ea-4f22-9b92-103b95ca8c0c" name="jetbrains.mps.lang.core">
      <concept id="1169194658468" name="jetbrains.mps.lang.core.structure.INamedConcept" flags="ngI" index="TrEIO">
        <property id="1169194664001" name="name" index="TrG5h" />
      </concept>
    </language>
  </registry>
  <node concept="1TIwiD" id="5bGVZ0_gaMm">
    <property role="EcuMT" value="5975414620309990550" />
    <property role="TrG5h" value="print" />
    <property role="R4oN_" value="Print a line to the console" />
    <ref role="1TJDcQ" node="5bGVZ0_gaMu" resolve="Statement" />
    <node concept="1TJgyj" id="1h$GaYG$t4I" role="1TKVEi">
      <property role="IQ2ns" value="1469493646990627118" />
      <property role="20lmBu" value="fLJjDmT/aggregation" />
      <property role="20kJfa" value="expression" />
      <property role="20lbJX" value="fLJekj4/_1" />
      <ref role="20lvS9" node="1h$GaYG$t4K" resolve="Expression" />
    </node>
  </node>
  <node concept="1TIwiD" id="5bGVZ0_gaMu">
    <property role="TrG5h" value="Statement" />
    <property role="EcuMT" value="5975414620309990558" />
  </node>
  <node concept="1TIwiD" id="1h$GaYG$t4K">
    <property role="TrG5h" value="Expression" />
    <property role="EcuMT" value="1469493646990627120" />
  </node>
</model>

