#!/usr/bin/env node

/**
 * Convert iBATIS/MyBatis 2.x .xsql files to MyBatis 3.x syntax.
 *
 * Usage:
 *   node convert-xsql-mybatis2-to-3.js /path/to/file.xsql
 *   node convert-xsql-mybatis2-to-3.js /path/to/input.xsql /path/to/output.xml
 */

const fs = require("fs");
const path = require("path");

function parseAttributes(attrText) {
  const attributes = {};
  const attrRegex = /([a-zA-Z_][\w:.-]*)\s*=\s*("([^"]*)"|'([^']*)')/g;
  let match;
  while ((match = attrRegex.exec(attrText)) !== null) {
    const key = match[1].trim();
    const value = (match[3] ?? match[4] ?? "").trim();
    attributes[key] = value;
  }
  return attributes;
}

function toOgnlLiteral(value) {
  if (value === undefined || value === null || value === "") {
    return "''";
  }
  const trimmed = String(value).trim();
  if (/^-?\d+(\.\d+)?$/.test(trimmed)) {
    return trimmed;
  }
  if (/^(true|false|null)$/i.test(trimmed)) {
    return trimmed.toLowerCase();
  }
  if (
    (trimmed.startsWith("'") && trimmed.endsWith("'")) ||
    (trimmed.startsWith('"') && trimmed.endsWith('"'))
  ) {
    return trimmed;
  }
  return "'" + trimmed.replace(/'/g, "\\'") + "'";
}

function getTestExpression(tagName, attrs) {
  const property = attrs.property || "/* property */";
  const compareValue = attrs.compareValue;
  const compareProperty = attrs.compareProperty;
  const compareTarget = compareProperty || toOgnlLiteral(compareValue);

  switch (tagName) {
    case "isNotNull":
      return property + " != null";
    case "isNull":
      return property + " == null";
    case "isNotEmpty":
      return property + " != null and " + property + " != ''";
    case "isEmpty":
      return property + " == null or " + property + " == ''";
    case "isEqual":
      return property + " != null and " + property + ".equals(" + compareTarget + ")";
    case "isNotEqual":
      return property + " == null or !" + property + ".equals(" + compareTarget + ")";
    case "isGreaterThan":
      return property + " != null and " + compareTarget + " != null and " + property + " > " + compareTarget;
    case "isGreaterEqual":
      return property + " != null and " + compareTarget + " != null and " + property + " >= " + compareTarget;
    case "isLessThan":
      return property + " != null and " + compareTarget + " != null and " + property + " < " + compareTarget;
    case "isLessEqual":
      return property + " != null and " + compareTarget + " != null and " + property + " <= " + compareTarget;
    default:
      return "/* TODO: condition */";
  }
}

function convertConditionalTags(sqlText) {
  const tagNameAlternation =
    "(isNotNull|isNull|isNotEmpty|isEmpty|isEqual|isNotEqual|isGreaterThan|isGreaterEqual|isLessThan|isLessEqual)";

  const openTagRegex = new RegExp("<\\s*" + tagNameAlternation + "\\b([^>]*)>", "gi");
  const closeTagRegex = new RegExp("<\\s*\\/\\s*" + tagNameAlternation + "\\s*>", "gi");

  let converted = sqlText.replace(openTagRegex, (_, rawTagName, rawAttrs) => {
    const attrs = parseAttributes(rawAttrs || "");
    const testExpression = getTestExpression(rawTagName, attrs);
    const prepend = attrs.prepend ? attrs.prepend + " " : "";
    return `<if test="${testExpression}">${prepend}`;
  });

  converted = converted.replace(closeTagRegex, "</if>");
  return converted;
}

function convertDynamicTag(sqlText) {
  const dynamicOpenTagRegex = /<\s*dynamic\b([^>]*)>/gi;
  const dynamicCloseTagRegex = /<\s*\/\s*dynamic\s*>/gi;

  let converted = sqlText.replace(dynamicOpenTagRegex, (_, rawAttrs) => {
    const attrs = parseAttributes(rawAttrs || "");
    const prefix = attrs.prepend || "";
    if (prefix) {
      return `<trim prefix="${prefix}" prefixOverrides="AND |OR ">`;
    }
    return `<trim prefixOverrides="AND |OR ">`;
  });

  converted = converted.replace(dynamicCloseTagRegex, "</trim>");
  return converted;
}

function convertStatementAttributes(sqlText) {
  return sqlText
    .replace(/\bparameterClass\s*=\s*("([^"]*)"|'([^']*)')/gi, "parameterType=$1")
    .replace(/\bresultClass\s*=\s*("([^"]*)"|'([^']*)')/gi, "resultType=$1");
}

function toLowerCamelFromUpperSnake(token) {
  const lowerCase = token.toLowerCase();
  return lowerCase.replace(/_([a-z0-9])/g, (_, ch) => ch.toUpperCase());
}

function mapArgumentToken(token) {
  const normalized = token.trim();
  if (/^[A-Z][A-Z0-9_]*$/.test(normalized) && normalized.includes("_")) {
    return "dto." + toLowerCamelFromUpperSnake(normalized);
  }
  return normalized;
}

function convertHashParameterSyntax(sqlText) {
  return sqlText.replace(/#\s*([a-zA-Z_][\w.\[\]]*)\s*#/g, (_, rawToken) => {
    const mappedToken = mapArgumentToken(rawToken);
    return `#{${mappedToken}}`;
  });
}

function convertDollarParameterSyntax(sqlText) {
  return sqlText.replace(/\$\s*([a-zA-Z_][\w.\[\]]*)\s*\$/g, (_, rawToken) => {
    const mappedToken = mapArgumentToken(rawToken);
    return "${" + mappedToken + "}";
  });
}

function toForeachItemExpression(expression, itemName) {
  const trimmed = expression.trim();
  const bracketMatch = trimmed.match(/^([a-zA-Z_][\w.]*)\[\]\.([a-zA-Z_][\w.]*)$/);
  if (bracketMatch) {
    return itemName + "." + bracketMatch[2];
  }
  const dottedMatch = trimmed.match(/^([a-zA-Z_][\w.]*)\.([a-zA-Z_][\w.]*)$/);
  if (dottedMatch) {
    return itemName + "." + dottedMatch[2];
  }
  return itemName;
}

function convertIterateBodyToForeach(body, property, itemName) {
  let result = body;
  const escapedProperty = property.replace(/[.*+?^${}()|[\]\\]/g, "\\$&");
  const escapedItem = itemName.replace(/[.*+?^${}()|[\]\\]/g, "\\$&");
  const dollarRegex = new RegExp("\\$\\s*(" + escapedProperty + "\\[\\](?:\\.[a-zA-Z_][\\w.]*)?)\\s*\\$", "g");
  const hashRegex = new RegExp("#\\s*(" + escapedProperty + "\\[\\](?:\\.[a-zA-Z_][\\w.]*)?)\\s*#", "g");

  result = result.replace(dollarRegex, (_, expr) => "${" + toForeachItemExpression(expr, itemName) + "}");
  result = result.replace(hashRegex, (_, expr) => "#{" + toForeachItemExpression(expr, itemName) + "}");
  result = result.replace(new RegExp("#\\s*" + escapedProperty + "\\[\\]\\s*#", "g"), "#{" + itemName + "}");
  result = result.replace(new RegExp("#\\s*" + escapedProperty + "\\s*#", "g"), "#{" + itemName + "}");
  result = result.replace(new RegExp("\\$\\s*" + escapedProperty + "\\s*\\$", "g"), "${" + itemName + "}");
  result = result.replace(new RegExp("#\\{\\s*" + escapedProperty + "\\s*\\}", "g"), "#{" + itemName + "}");
  result = result.replace(new RegExp("\\$\\{\\s*" + escapedProperty + "\\s*\\}", "g"), "${" + itemName + "}");
  result = result.replace(new RegExp("#\\{\\s*" + escapedProperty + "\\s*\\.\\s*([a-zA-Z_][\\w.]*)\\s*\\}", "g"), (_, suffix) => {
    return "#{" + itemName + "." + suffix + "}";
  });
  result = result.replace(new RegExp("\\$\\{\\s*" + escapedProperty + "\\s*\\.\\s*([a-zA-Z_][\\w.]*)\\s*\\}", "g"), (_, suffix) => {
    return "${" + itemName + "." + suffix + "}";
  });
  result = result.replace(new RegExp("#\\{\\s*" + escapedItem + "\\s*\\}", "g"), "#{" + itemName + "}");
  result = result.replace(new RegExp("\\$\\{\\s*" + escapedItem + "\\s*\\}", "g"), "${" + itemName + "}");
  return result;
}

function convertIterateTag(sqlText) {
  const iterateBlockRegex = /<\s*iterate\b([^>]*)>([\s\S]*?)<\s*\/\s*iterate\s*>/gi;
  return sqlText.replace(iterateBlockRegex, (_, rawAttrs, body) => {
    const attrs = parseAttributes(rawAttrs || "");
    const property = attrs.property || "list";
    const conjunction = Object.prototype.hasOwnProperty.call(attrs, "conjunction")
      ? attrs.conjunction
      : ",";
    const itemName = attrs.item || "item";
    const open = attrs.open ? ` open="${attrs.open}"` : "";
    const close = attrs.close ? ` close="${attrs.close}"` : "";
    const convertedBody = convertIterateBodyToForeach(body, property, itemName);
    return `<foreach collection="${property}" item="${itemName}" separator="${conjunction}"${open}${close}>${convertedBody}</foreach>`;
  });
}

function convertMyBatis2to3(input) {
  let output = input;
  output = convertStatementAttributes(output);
  output = convertDynamicTag(output);
  output = convertConditionalTags(output);
  output = convertIterateTag(output);
  output = convertHashParameterSyntax(output);
  output = convertDollarParameterSyntax(output);
  return output;
}

function printUsageAndExit() {
  const scriptName = path.basename(process.argv[1]);
  console.error("Usage:");
  console.error(`  node ${scriptName} <input.xsql> [output.xml]`);
  process.exit(1);
}

function main() {
  const [, , inputPathArg, outputPathArg] = process.argv;
  if (!inputPathArg) {
    printUsageAndExit();
  }

  const inputPath = path.resolve(process.cwd(), inputPathArg);
  const outputPath = outputPathArg
    ? path.resolve(process.cwd(), outputPathArg)
    : path.resolve(path.dirname(inputPath), path.basename(inputPath, path.extname(inputPath)) + ".mybatis3.xml");

  const original = fs.readFileSync(inputPath, "utf8");
  const converted = convertMyBatis2to3(original);
  fs.writeFileSync(outputPath, converted, "utf8");

  console.log(`Converted: ${inputPath}`);
  console.log(`Output   : ${outputPath}`);
}

main();
