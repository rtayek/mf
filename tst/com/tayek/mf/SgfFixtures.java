package com.tayek.mf;

import java.util.LinkedHashMap;
import java.util.Map;

/** Small SGF examples adapted from rtayek/rtgo src/sgf/Parser.java. */
final class SgfFixtures {
    private SgfFixtures() { }

    static Map<String, String> valid() {
        Map<String, String> cases = new LinkedHashMap<>();
        cases.put("startOfGame", "(;GM[1]FF[4]VW[]CA[UTF-8])");
        cases.put("oneMoveAtA1", "(;FF[4];B[as])");
        cases.put("comments1", "(;C[root];C[left1];C[left1.left2](;C[left1.right1])(;C[right1]))");
        cases.put("noVariation", "(;FF[4]GM[1]SZ[19];B[aa];W[bb];B[cc];W[dd];B[ad];W[bd])");
        cases.put("simpleWithVariations", "(;FF[4]C[root](;B[aa]C[a];C[b]W[bb](;C[c]B[cc])(;C[d]B[dd];C[e]W[ee]))(;C[f]B[ff](;C[g]W[gg];C[h]B[hh];C[i]W[ii])(;C[j]W[jj];)))");
        cases.put("twoVariationsAtMoveThree", "(;FF[4]GM[1]SZ[19];B[aa];W[bb](;B[cc]N[Var A];W[dd];B[ad];W[bd])(;B[hh]N[Var B];W[hg])(;B[gg]N[Var C];W[gh];B[hh];W[hg];B[kk]))");
        cases.put("variationOfAVariation", "(;FF[4]GM[1]SZ[19];B[aa];W[bb](;B[cc]N[Var A];W[dd];B[ad];W[bd])(;B[hh]N[Var B];W[hg])(;B[gg]N[Var C];W[gh];B[hh](;W[hg]N[Var A];B[kk])(;W[kl]N[Var B])))");
        cases.put("newvariationssmall", "(;GM[1]FF[4](;B[qd]BL[897]WL[900];W[oc]BL[897]WL[886];B[ld]BL[878]WL[886])(;B[pd]BL[897]WL[891]))");
        cases.put("smartgovariationsflat", "(;GM[1]FF[4]SZ[19](;B[qd](;W[oc];B[ld])(;W[od];B[oc]))(;B[pd]))");
        cases.put("twoGamesInOneFileFromSmartGo", "(;GM[1]FF[4]SZ[19]AP[SmartGo:3.1.8]PW[ray]PB[SmartGo]DT[2022-01-07]KM[6.5]RU[Simple]TM[1800.0]OT[20 / 5]BL[1800.0]OM[20]OP[300.0];B[qd]V[0.0]BL[1799.6];W[oq];B[dd]V[0.0]BL[1799.5];W[oc];B[dp]V[0.0]BL[1799.4])(;GM[1]FF[4]SZ[19]AP[SmartGo:3.1.8]PW[raz]PB[SmartGo]DT[2022-01-07]KM[6.5]RU[Simple]TM[1800.0]OT[20 / 5]BL[1800.0]OM[20]OP[300.0];B[dq]V[0.0]BL[1799.6];W[oq];B[dd]V[0.0]BL[1799.5];W[oc];B[dp]V[0.0]BL[1799.4])");
        cases.put("smartgo4", "(;GM[1])\n(;GM[2])\n(;GM[3])\n(;GM[4])");
        cases.put("setupAndMarks", "(;GM[1]SZ[9]AB[aa][bb]AW[cc]PL[W]TR[dd][ee]LB[ff:A]C[setup])");
        cases.put("escapedComment", "(;GM[1]C[backslash \\\\ and bracket \\] and more]XX[a][b])");
        cases.put("sgfExamleFromRedBean", "(;FF[4]C[root](;C[a];C[b](;C[c])\n(;C[d];C[e]))\n(;C[f](;C[g];C[h];C[i])\n(;C[j])))");
        cases.put("oneMoveAtA1NoHeader", "(;B[as])");
        cases.put("simplevariations", "(;GM[1]FF[4]CA[UTF-8]AP[Many Faces of Go:12.024]SZ[19](;B[qd]BL[892]WL[900](;W[oc]BL[892]WL[881])(;W[od]BL[888]WL[881];B[oc]BL[883]WL[881]))(;B[pq]BL[892]WL[890]))");
        cases.put("consecutiveMoves", "(;SZ[19];B[as];B[ar])");
        cases.put("emptyWithSemicolon", "(;)");
        cases.put("twoEmptyWithSemicolon", "(;)\n(;)");
        cases.put("oneVariationAtMoveThree", "(;FF[4]GM[1]SZ[19];B[aa];W[bb](;B[cc];W[dd];B[ad];W[bd])(;B[hh];W[hg]))");
        cases.put("twoVariationsAtDifferentMoves", "(;FF[4]GM[1]SZ[19];B[aa];W[bb](;B[cc];W[dd](;B[ad];W[bd])(;B[ee];W[ff]))(;B[hh];W[hg]))");
        cases.put("manyFacesTwoMovesAtA1AndR16", "(;GM[1]FF[4]VW[]AP[Many Faces of Go:12.022]SZ[19]HA[0]ST[0]PB[ray]PW[ray]DT[2015-03-31]KM[7.5]RU[Chinese]BR[2 Dan]WR[2 Dan];B[as]BL[50]WL[60];W[qd]BL[1800]WL[1727])");
        cases.put("manyFacesTwoMovesAtA1AndR16OnA9by9Board", "(;GM[1]FF[4]VW[]AP[Many Faces of Go:12.022]SZ[9]HA[0]ST[0]PB[ray]PW[ray]DT[2015-04-12]KM[7.5]RU[Chinese]BR[2 Dan]WR[2 Dan];B[ai]BL[58]WL[60];W[gd]BL[29]WL[50])");
        cases.put("newvariationsmfflat", "(;GM[1]FF[4]VW[]CA[UTF-8]AP[Many Faces of Go:12.024]SZ[19]HA[0]ST[0]PB[Opponent]PW[Opponent]DT[2022-03-28]KM[6.5]RU[Japanese]BR[30 Kyu]WR[30 Kyu];B[qd]BL[895]WL[900](;W[od]BL[895]WL[897];B[oc]BL[891]WL[897];W[nc]BL[891]WL[896];B[pc]BL[890]WL[896](;W[nd]BL[890]WL[893];B[qf]BL[889]WL[893];W[jc]BL[889]WL[891])(;W[md]BL[889]WL[891];B[pe]BL[886]WL[891];W[ic]BL[886]WL[888]))(;W[oc]BL[880]WL[888];B[ld]BL[875]WL[888];W[of]BL[875]WL[886](;B[qg]BL[873]WL[886])(;B[oe]BL[873]WL[883];W[ne]BL[873]WL[883];B[pe]BL[872]WL[883];W[nd]BL[872]WL[881];B[nf]BL[870]WL[881];W[mf]BL[870]WL[880];B[ng]BL[869]WL[880];W[le]BL[869]WL[879];B[og]BL[867]WL[879];W[kd]BL[867]WL[878])))");
        cases.put("twoverysmallgamesflat", "(;B[as])\n(;B[at])");
        cases.put("twosmallgamesflat", "(;FF[4];B[as])\n(;FF[4];B[at])");
        cases.put("smartgo42", "(;GM[1];B[as])\n(;GM[2];B[as])\n(;GM[3];B[as])\n(;GM[4];B[as])");
        cases.put("smartgo43", "(;GM[1];B[as];B[at])\n(;GM[2];B[as];B[at])\n(;GM[3];B[as];B[at])\n(;GM[4];B[as];B[at])");
        return java.util.Collections.unmodifiableMap(cases);
    }
}
