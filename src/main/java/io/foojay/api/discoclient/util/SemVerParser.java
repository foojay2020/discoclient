/*
 * Copyright (c) 2021, Azul
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * - Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer
 *   in the documentation and/or other materials provided with the distribution.
 * - Neither the name of Azul nor the names of its contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL AZUL BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY,
 * OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package io.foojay.api.discoclient.util;

import io.foojay.api.discoclient.pkg.SemVer;
import io.foojay.api.discoclient.pkg.VersionNumber;

import java.util.List;
import java.util.Scanner;
import java.util.function.Predicate;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class SemVerParser {
    private static final Pattern SEM_VER_PATTERN = Pattern.compile("^(<|<=|>|>=|=)?v?([0-9]+)(\\.[0-9]+)?(\\.[0-9]+)?(\\.[0-9]+)?(\\.[0-9]+)?(\\.[0-9]+)?(-([0-9A-Za-z\\-]+(\\.[0-9A-Za-z\\-]+)*))?(\\+([0-9A-Za-z\\-]+(\\.[0-9A-Za-z\\-]+)*))?((<|<=|>|>=|=)?v?([0-9]+)(\\.[0-9]+)?(\\.[0-9]+)?(\\.[0-9]+)?(\\.[0-9]+)?(\\.[0-9]+)?(-([0-9A-Za-z\\-]+(\\.[0-9A-Za-z\\-]+)*))?(\\+([0-9A-Za-z\\-]+(\\.[0-9A-Za-z\\-]+)*))?)?$");


    public static SemVerParsingResult fromText(final String text) {
        SemVerParsingResult parsingResult = new SemVerParsingResult();

        // ******************** Parsing 1st SemVer ****************************

        // Remove leading "1." to get correct version number e.g. 1.8u262 -> 8u262
        String versionText1 = text.startsWith("1.") ? text.replace("1.", "") : text;
        if (versionText1.contains("_")) {
            versionText1 = versionText1.replace("_", ".");
        }
        if (versionText1.matches("[0-9]+u[0-9]+.*")) {
            versionText1 = versionText1.replace("u", ".0.");
        }

        //final Matcher           semverMatcher = SEM_VER_PATTERN.matcher(versionText1);
        //final List<MatchResult> results       = semverMatcher.results().collect(Collectors.toList());

        final List<MatchResult> results;
        try(Scanner s = new Scanner(versionText1)) {
            results = Helper.findAll(s, SEM_VER_PATTERN).collect(Collectors.toList());
        }

        if (results.isEmpty()) {
            parsingResult.setError1(new Error("Invalid semver: " + versionText1));
            return parsingResult;
        }

        MatchResult result = results.get(0);

        String metadata1 = null != result.group(12) ? result.group(12) : "";
        String pre1      = null != result.group(9)  ? result.group(9)  : "";

        if (pre1.equals("ea.0")) { pre1 = "ea"; }

        VersionNumber versionNumber1 = new VersionNumber();

        Comparison comparison1;
        if (null == result.group(1)) {
            comparison1 = Comparison.EQUAL;
        } else {
            comparison1 = Comparison.fromText(result.group(1));
        }

        // 1st number
        try {
            if (null == result.group(2)) {
                parsingResult.setError1(new Error("Feature version cannot be null"));
                return parsingResult;
            }
            versionNumber1.setFeature(Integer.parseInt(result.group(2)));
        } catch (NumberFormatException e) {
            parsingResult.setError1(new Error("Error when parsing feature version " + result.group(2) + ": " + e));
            return parsingResult;
        }

        // 2nd number
        try {
            if (null == result.group(3)) {
                versionNumber1.setInterim(0);
            } else {
                versionNumber1.setInterim(Integer.parseInt(Helper.trimPrefix(result.group(3), "\\.")));
            }
        } catch (NumberFormatException e) {
            parsingResult.setError1(new Error("Error when parsing interim version " + result.group(3) + ": " + e));
            return parsingResult;
        }

        // 3rd number
        try {
            if (null == result.group(4)) {
                versionNumber1.setUpdate(0);
            } else {
                versionNumber1.setUpdate(Integer.parseInt(Helper.trimPrefix(result.group(4), "\\.")));
            }
        } catch (NumberFormatException e) {
            parsingResult.setError1(new Error("Error when parsing update version " + result.group(4) + ": " + e));
            return parsingResult;
        }

        // 4th number
        try {
            if (null == result.group(5)) {
                versionNumber1.setPatch(0);
            } else {
                versionNumber1.setPatch(Integer.parseInt(Helper.trimPrefix(result.group(5), "\\.")));
            }
        } catch (NumberFormatException e) {
            parsingResult.setError1(new Error("Error when parsing patch version " + result.group(5) + ": " + e));
            return parsingResult;
        }

        // 5th number
        try {
            if (null == result.group(6)) {
                versionNumber1.setFifth(0);
            } else {
                versionNumber1.setFifth(Integer.parseInt(Helper.trimPrefix(result.group(6), "\\.")));
            }
        } catch (NumberFormatException e) {
            parsingResult.setError1(new Error("Error when parsing fifth number " + result.group(6) + ": " + e));
            return parsingResult;
        }

        // 6th number
        try {
            if (null == result.group(7)) {
                versionNumber1.setSixth(0);
            } else {
                versionNumber1.setSixth(Integer.parseInt(Helper.trimPrefix(result.group(7), "\\.")));
            }
        } catch (NumberFormatException e) {
            parsingResult.setError1(new Error("Error when parsing sixth number " + result.group(7) + ": " + e));
            return parsingResult;
        }

        // Validate prerelease
        Error err1;
        if (null != pre1 && !pre1.isEmpty()) {
            err1 = validatePrerelease(pre1);
            if (null != err1) {
                parsingResult.setError1(err1);
                return parsingResult;
            }
        }

        // Validate metadata
        if (null != metadata1 && !metadata1.isEmpty()) {
            err1 = validateMetadata(metadata1);
            if (null != err1) {
                parsingResult.setError1(err1);
                return parsingResult;
            }
        }
        SemVer semVer1 = new SemVer(versionNumber1, pre1, metadata1);
        semVer1.setComparison(comparison1);
        parsingResult.setSemVer1(semVer1);

        Predicate<SemVer> filter = null;

        // ******************** Parsing 2nd SemVer ****************************
        if (result.groupCount() == 27 && null != result.group(14)) {
            String metadata2 = null != result.group(26) ? result.group(26) : "";
            String pre2      = null != result.group(23) ? result.group(23) : "";

            if (pre2.equals("ea.0")) { pre2 = "ea"; }

            VersionNumber versionNumber2 = new VersionNumber();

            Comparison comparison2;
            if (null == result.group(15)) {
                comparison2 = Comparison.EQUAL;
            } else {
                comparison2 = Comparison.fromText(result.group(15));
            }

            // 1st number
            boolean oldFormat;
            try {
                if (null == result.group(16)) {
                    parsingResult.setError2(new Error("Feature version cannot be null"));
                    return parsingResult;
                }
                oldFormat = Integer.parseInt(result.group(16)) == 1;
                versionNumber2.setFeature(Integer.parseInt(result.group(16)));
            } catch (NumberFormatException e) {
                parsingResult.setError2(new Error("Error when parsing feature version " + result.group(16) + ": " + e));
                return parsingResult;
            }

            // 2nd number
            try {
                if (null == result.group(17)) {
                    versionNumber2.setInterim(0);
                } else {
                    versionNumber2.setInterim(Integer.parseInt(Helper.trimPrefix(result.group(17), "\\.")));
                }
            } catch (NumberFormatException e) {
                parsingResult.setError2(new Error("Error when parsing interim version " + result.group(17) + ": " + e));
                return parsingResult;
            }

            // 3rd number
            try {
                if (null == result.group(18)) {
                    versionNumber2.setUpdate(0);
                } else {
                    versionNumber2.setUpdate(Integer.parseInt(Helper.trimPrefix(result.group(18), "\\.")));
                }
            } catch (NumberFormatException e) {
                parsingResult.setError2(new Error("Error when parsing update version " + result.group(18) + ": " + e));
                return parsingResult;
            }

            // 4th number
            try {
                if (null == result.group(19)) {
                    versionNumber2.setPatch(0);
                } else {
                    versionNumber2.setPatch(Integer.parseInt(Helper.trimPrefix(result.group(19), "\\.")));
                }
            } catch (NumberFormatException e) {
                parsingResult.setError2(new Error("Error when parsing patch version " + result.group(19) + ": " + e));
                return parsingResult;
            }

            // 5th number
            try {
                if (null == result.group(20)) {
                    versionNumber2.setFifth(0);
                } else {
                    versionNumber2.setFifth(Integer.parseInt(Helper.trimPrefix(result.group(20), "\\.")));
                }
            } catch (NumberFormatException e) {
                parsingResult.setError2(new Error("Error when parsing fifth number " + result.group(20) + ": " + e));
                return parsingResult;
            }

            // 6th number
            try {
                if (null == result.group(21)) {
                    versionNumber2.setSixth(0);
                } else {
                    versionNumber2.setSixth(Integer.parseInt(Helper.trimPrefix(result.group(21), "\\.")));
                }
            } catch (NumberFormatException e) {
                parsingResult.setError2(new Error("Error when parsing sixth number " + result.group(21) + ": " + e));
                return parsingResult;
            }

            // Remove leading "1." to get correct version number e.g. 1.8u262 -> 8u262
            if (oldFormat) {
                versionNumber2.setFeature(versionNumber2.getInterim().getAsInt());
                versionNumber2.setInterim(versionNumber2.getUpdate().getAsInt());
                versionNumber2.setUpdate(versionNumber2.getPatch().getAsInt());
                versionNumber2.setPatch(0);
            }

            // Validate prerelease
            Error err2;
            if (null != pre2 && !pre2.isEmpty()) {
                err2 = validatePrerelease(pre2);
                if (null != err2) {
                    parsingResult.setError2(err2);
                    return parsingResult;
                }
            }

            // Validate metadata
            if (null != metadata2 && !metadata2.isEmpty()) {
                err2 = validateMetadata(metadata2);
                if (null != err2) {
                    parsingResult.setError2(err2);
                    return parsingResult;
                }
            }
            SemVer semVer2 = new SemVer(versionNumber2, pre2, metadata2);
            semVer2.setComparison(comparison2);

            // Define filter
            switch(comparison1) {
                case LESS_THAN:
                    filter = semVer -> semVer.lessThan(semVer1); break;
                case LESS_THAN_OR_EQUAL:
                    filter = semVer -> (semVer.lessThan(semVer1) || semVer.equalTo(semVer1)); break;
                case GREATER_THAN:
                    switch(comparison2) {
                        case LESS_THAN         : filter = semVer -> semVer.greaterThan(semVer1) && semVer.lessThan(semVer2); break;
                        case LESS_THAN_OR_EQUAL: filter = semVer -> semVer.greaterThan(semVer1) && (semVer.lessThan(semVer2) || semVer.equalTo(semVer2)); break;
                        default                : filter = semVer -> semVer.greaterThan(semVer1); break;
                    }
                    break;
                case GREATER_THAN_OR_EQUAL:
                    switch(comparison2) {
                        case LESS_THAN         : filter = semVer -> (semVer.equalTo(semVer1) || semVer.greaterThan(semVer1)) && semVer.lessThan(semVer2); break;
                        case LESS_THAN_OR_EQUAL: filter = semVer -> (semVer.equalTo(semVer1) || semVer.greaterThan(semVer1)) && (semVer.lessThan(semVer2) || semVer.equalTo(semVer2)); break;
                        default                : filter = semVer -> (semVer.equalTo(semVer1) || semVer.greaterThan(semVer1)); break;
                    }
                    break;
            }
            parsingResult.setFilter(filter);

            parsingResult.setSemVer2(semVer2);
            return parsingResult;
        }

        // Define filter
        switch(comparison1) {
            case LESS_THAN            : filter = semVer -> semVer.lessThan(semVer1); break;
            case LESS_THAN_OR_EQUAL   : filter = semVer -> (semVer.lessThan(semVer1) || semVer.equalTo(semVer1)); break;
            case GREATER_THAN         : filter = semVer -> semVer.greaterThan(semVer1); break;
            case GREATER_THAN_OR_EQUAL: filter = semVer -> (semVer.equalTo(semVer1) || semVer.greaterThan(semVer1)); break;
        }
        parsingResult.setFilter(filter);

        return parsingResult;
    }

    private static Error validatePrerelease(final String preRelease) {
        String[] eparts = preRelease.split("\\.");
        for (String p : eparts) {
            if (p.matches("[0-9]+")) {
                if (p.length() > 0 && p.startsWith("0")) {
                    return new Error("Segment starts with 0: " + p);
                }
            } else if (!p.matches("[a-zA-Z-0-9]+")) {
                return new Error("Invalid preRelease: " + preRelease);
            }
        }
        return null;
    }

    private static Error validateMetadata(final String metadata) {
        String[] eparts = metadata.split("\\.");
        for (String p : eparts) {
            if (!p.matches("[a-zA-Z-0-9]+")) {
                return new Error("Invalid metadata: " + metadata);
            }
        }
        return null;
    }
}