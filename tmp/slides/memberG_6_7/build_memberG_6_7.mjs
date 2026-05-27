import { Presentation, PresentationFile } from "@oai/artifact-tool";
import fs from "node:fs/promises";

const W = 1280;
const H = 720;

const FONT_TITLE = "Microsoft YaHei";
const FONT_BODY = "Microsoft YaHei";

const presentation = Presentation.create({
  slideSize: { width: W, height: H },
});

presentation.theme.colorScheme = {
  name: "MemberGTheme",
  themeColors: {
    accent1: "#1F4E79",
    accent2: "#2F7FD3",
    accent3: "#0EA5A4",
    accent4: "#F59E0B",
    accent5: "#334155",
    accent6: "#94A3B8",
    bg1: "#F7FAFC",
    bg2: "#EAF1F8",
    tx1: "#0F172A",
    tx2: "#475569",
    hlink: "#2563EB",
    folHlink: "#7C3AED",
  },
};

function addHeader(slide, pageLabel, title, subtitle) {
  const bar = slide.shapes.add({
    geometry: "rect",
    position: { left: 0, top: 0, width: W, height: H },
    fill: {
      type: "solid",
      color: "#F5F8FC",
    },
    line: { width: 0, style: "solid", fill: "#F5F8FC" },
  });

  const topBand = slide.shapes.add({
    geometry: "rect",
    position: { left: 0, top: 0, width: W, height: 86 },
    fill: {
      type: "gradient",
      angle: 0,
      stops: [
        { offset: 0, color: "#1F4E79" },
        { offset: 100000, color: "#2563A6" },
      ],
    },
    line: { width: 0, style: "solid", fill: "#1F4E79" },
  });

  const label = slide.shapes.add({
    geometry: "roundRect",
    position: { left: 36, top: 24, width: 126, height: 38 },
    fill: "#F59E0B",
    line: { width: 0, style: "solid", fill: "#F59E0B" },
  });
  label.text = pageLabel;
  label.text.fontSize = 20;
  label.text.bold = true;
  label.text.color = "#FFFFFF";
  label.text.typeface = FONT_TITLE;
  label.text.alignment = "center";
  label.text.verticalAlignment = "middle";

  const t = slide.shapes.add({
    geometry: "rect",
    position: { left: 180, top: 18, width: 1030, height: 44 },
    fill: { type: "solid", color: "#00000000" },
    line: { width: 0, style: "solid", fill: "#00000000" },
  });
  t.text = title;
  t.text.fontSize = 34;
  t.text.bold = true;
  t.text.color = "#FFFFFF";
  t.text.typeface = FONT_TITLE;
  t.text.verticalAlignment = "middle";

  const st = slide.shapes.add({
    geometry: "rect",
    position: { left: 182, top: 62, width: 1010, height: 24 },
    fill: { type: "solid", color: "#00000000" },
    line: { width: 0, style: "solid", fill: "#00000000" },
  });
  st.text = subtitle;
  st.text.fontSize = 16;
  st.text.color = "#DBEAFE";
  st.text.typeface = FONT_BODY;
}

function addCard(slide, { left, top, width, height, title, bullets, accent = "#2F7FD3" }) {
  const card = slide.shapes.add({
    geometry: "roundRect",
    position: { left, top, width, height },
    fill: "#FFFFFF",
    line: { style: "solid", fill: "#D6E2F1", width: 1.2 },
  });

  const stripe = slide.shapes.add({
    geometry: "rect",
    position: { left: left + 14, top: top + 14, width: 8, height: 34 },
    fill: accent,
    line: { width: 0, style: "solid", fill: accent },
  });

  const t = slide.shapes.add({
    geometry: "rect",
    position: { left: left + 30, top: top + 12, width: width - 40, height: 40 },
    fill: { type: "solid", color: "#00000000" },
    line: { width: 0, style: "solid", fill: "#00000000" },
  });
  t.text = title;
  t.text.typeface = FONT_TITLE;
  t.text.bold = true;
  t.text.fontSize = 24;
  t.text.color = "#0F172A";

  const b = slide.shapes.add({
    geometry: "rect",
    position: { left: left + 28, top: top + 58, width: width - 42, height: height - 74 },
    fill: { type: "solid", color: "#00000000" },
    line: { width: 0, style: "solid", fill: "#00000000" },
  });
  b.text = bullets.map((x) => `• ${x}`);
  b.text.typeface = FONT_BODY;
  b.text.fontSize = 18;
  b.text.color = "#334155";
  b.text.spacingAfter = 200;
}

function addBottomNote(slide, text) {
  const note = slide.shapes.add({
    geometry: "roundRect",
    position: { left: 46, top: 642, width: 1188, height: 50 },
    fill: "#E8F1FB",
    line: { style: "solid", fill: "#BDD3EA", width: 1 },
  });
  note.text = `结论：${text}`;
  note.text.typeface = FONT_BODY;
  note.text.fontSize = 19;
  note.text.bold = true;
  note.text.color = "#0F3B66";
  note.text.verticalAlignment = "middle";
  note.text.insets = { left: 16, right: 16, top: 8, bottom: 8 };
}

// Slide 6
const s6 = presentation.slides.add();
addHeader(
  s6,
  "第6页",
  "马克思主义解释线（一）",
  "第一原理：生产力决定生产关系"
);

addCard(s6, {
  left: 46,
  top: 116,
  width: 576,
  height: 250,
  title: "历史唯物主义的规律",
  bullets: [
    "蒸汽机、电力、互联网每次跃迁都重塑协作关系与分配方式",
    "技术革命不是“旧岗位升级”，而是“职业生态重组”",
    "因此不能只讨论培训提效，更要讨论关系重构",
  ],
  accent: "#2F7FD3",
});

addCard(s6, {
  left: 658,
  top: 116,
  width: 576,
  height: 250,
  title: "Agent 时代的质变",
  bullets: [
    "Agent可直接产出代码、报告、研究等市场化成果",
    "变化不是量变提效，而是价值生产主体的结构变化",
    "“执行层被替代”说明旧生产关系已难容纳新生产力",
  ],
  accent: "#0EA5A4",
});

addCard(s6, {
  left: 46,
  top: 388,
  width: 1188,
  height: 230,
  title: "对 Hinton 观点的马克思主义回应",
  bullets: [
    "Hinton看到了替代风险，但主要停留在监管、限速、补贴等旧框架修补",
    "问题核心不在“堵洪水”，而在“修河道”：主动设计适配Agent的新生产关系",
    "顺着“生产力→生产关系”逻辑继续追问：新的协作结构与价值分配应如何重建",
  ],
  accent: "#F59E0B",
});

addBottomNote(s6, "当生产力发生质变，生产关系不可能原地不动。AI时代需要的是制度与协作方式升级。"
);

// Slide 7
const s7 = presentation.slides.add();
addHeader(
  s7,
  "第7页",
  "马克思主义解释线（二）",
  "执行劳动被压缩，意图劳动上移为核心"
);

addCard(s7, {
  left: 46,
  top: 116,
  width: 576,
  height: 262,
  title: "变化一：边际劳动成本被重写",
  bullets: [
    "传统工具每次调用都需新增人工投入",
    "Agent把大量劳动“前置压缩”到模型权重与知识库",
    "调用1次与100万次的边际成本趋同，规模效应空前放大",
  ],
  accent: "#2563EB",
});

addCard(s7, {
  left: 658,
  top: 116,
  width: 576,
  height: 262,
  title: "变化二：劳动重心从执行转向意图",
  bullets: [
    "Agent主要替代“具体执行层”，并未替代价值判断",
    "人类角色转向：意图定义、架构判断、审美与伦理裁决",
    "核心竞争力由“写得快”转为“定义对问题与目标”",
  ],
  accent: "#14B8A6",
});

addCard(s7, {
  left: 46,
  top: 402,
  width: 1188,
  height: 216,
  title: "落到课堂结论：我们应如何理解这场变化",
  bullets: [
    "把“AI替代焦虑”上升为“生产关系重构问题”，才是马克思主义视角的关键推进",
    "就业与分工不会消失，而是围绕Agent重排：人机协作、流程编排、意图翻译成为新岗位核心",
    "因此，第7页结论是：真正被重塑的是分工结构与价值分配逻辑，而非单一岗位的存亡",
  ],
  accent: "#F59E0B",
});

addBottomNote(s7, "人的不可替代性正在“上移”到意图、判断与责任层，这正是新生产关系的起点。"
);

const outDir = "D:/project/JAVA_PROJECT/ragent/outputs";
await fs.mkdir(outDir, { recursive: true });
const outPath = `${outDir}/成员G_第6-7页.pptx`;
const pptx = await PresentationFile.exportPptx(presentation);
await pptx.save(outPath);
console.log(outPath);
