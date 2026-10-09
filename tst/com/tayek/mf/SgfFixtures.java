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
        return Map.copyOf(cases);
    }
}
