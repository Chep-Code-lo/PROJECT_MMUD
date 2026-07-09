from __future__ import annotations

from math import atan2, cos, sin
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont


ROOT = Path(__file__).resolve().parents[1]
OUTPUT_DIR = ROOT / "docs" / "security"
FONT_DIR = Path(r"C:\Windows\Fonts")

WIDTH = 1400
HEIGHT = 1560

COLORS = {
    "background": "#F2F2F2",
    "panel": "#FFFFFF",
    "ink": "#111111",
    "border": "#111111",
}


def load_font(size: int, *, bold: bool = False) -> ImageFont.FreeTypeFont:
    candidates = ["segoeuib.ttf", "arialbd.ttf"] if bold else ["segoeui.ttf", "arial.ttf"]
    for name in candidates:
        path = FONT_DIR / name
        if path.exists():
            return ImageFont.truetype(str(path), size=size)
    raise FileNotFoundError("Khong tim thay font he thong phu hop.")


TITLE_FONT = load_font(34, bold=True)
BOX_TITLE_FONT = load_font(28, bold=True)
BODY_FONT = load_font(20)
SMALL_FONT = load_font(18)
LAYER_TITLE_FONT = load_font(26, bold=True)
LAYER_COL_TITLE_FONT = load_font(20, bold=True)
LABEL_FONT = load_font(18, bold=True)


def text_width(draw: ImageDraw.ImageDraw, text: str, font: ImageFont.FreeTypeFont) -> int:
    left, _, right, _ = draw.textbbox((0, 0), text, font=font)
    return right - left


def line_height(font: ImageFont.FreeTypeFont) -> int:
    ascent, descent = font.getmetrics()
    return ascent + descent


def wrap_text(
    draw: ImageDraw.ImageDraw,
    text: str,
    font: ImageFont.FreeTypeFont,
    max_width: int,
) -> list[str]:
    lines: list[str] = []
    for paragraph in text.split("\n"):
        words = paragraph.split()
        if not words:
            lines.append("")
            continue
        current = words[0]
        for word in words[1:]:
            candidate = f"{current} {word}"
            if text_width(draw, candidate, font) <= max_width:
                current = candidate
            else:
                lines.append(current)
                current = word
        lines.append(current)
    return lines


def draw_text_block(
    draw: ImageDraw.ImageDraw,
    box: tuple[int, int, int, int],
    text: str,
    font: ImageFont.FreeTypeFont,
    *,
    align: str = "center",
    valign: str = "middle",
    fill: str = COLORS["ink"],
    line_gap: int = 8,
) -> None:
    x1, y1, x2, y2 = box
    lines = wrap_text(draw, text, font, max(40, x2 - x1))
    row_height = line_height(font) + line_gap
    total_height = max(0, len(lines) * row_height - line_gap)

    if valign == "top":
        y = y1
    else:
        y = y1 + max(0, (y2 - y1 - total_height) // 2)

    for line in lines:
        width = text_width(draw, line, font)
        if align == "left":
            x = x1
        elif align == "right":
            x = x2 - width
        else:
            x = x1 + max(0, (x2 - x1 - width) // 2)
        draw.text((x, y), line, font=font, fill=fill)
        y += row_height


def draw_panel(draw: ImageDraw.ImageDraw, box: tuple[int, int, int, int], *, width: int = 3) -> None:
    draw.rectangle(box, fill=COLORS["panel"], outline=COLORS["border"], width=width)


def draw_ellipse_panel(draw: ImageDraw.ImageDraw, box: tuple[int, int, int, int], *, width: int = 3) -> None:
    draw.ellipse(box, fill=COLORS["panel"], outline=COLORS["border"], width=width)


def draw_arrow(
    draw: ImageDraw.ImageDraw,
    start: tuple[int, int],
    end: tuple[int, int],
    *,
    width: int = 4,
    label: str | None = None,
    label_offset: tuple[int, int] = (0, 0),
) -> None:
    draw.line((start, end), fill=COLORS["border"], width=width)
    angle = atan2(end[1] - start[1], end[0] - start[0])
    head = 16
    wing = 9
    left = (
        end[0] - head * cos(angle) + wing * sin(angle),
        end[1] - head * sin(angle) - wing * cos(angle),
    )
    right = (
        end[0] - head * cos(angle) - wing * sin(angle),
        end[1] - head * sin(angle) + wing * cos(angle),
    )
    draw.polygon([end, left, right], fill=COLORS["border"])

    if label:
        mid_x = (start[0] + end[0]) // 2 + label_offset[0]
        mid_y = (start[1] + end[1]) // 2 + label_offset[1]
        label_width = text_width(draw, label, LABEL_FONT) + 22
        label_height = line_height(LABEL_FONT) + 10
        label_box = (
            mid_x - label_width // 2,
            mid_y - label_height // 2,
            mid_x + label_width // 2,
            mid_y + label_height // 2,
        )
        draw.rectangle(label_box, fill=COLORS["background"])
        draw_text_block(draw, label_box, label, LABEL_FONT, line_gap=2)


def draw_box(
    draw: ImageDraw.ImageDraw,
    box: tuple[int, int, int, int],
    title: str,
    body: str | None = None,
) -> None:
    draw_panel(draw, box)
    x1, y1, x2, y2 = box
    if body:
        title_box = (x1 + 24, y1 + 16, x2 - 24, y1 + 54)
        body_box = (x1 + 28, y1 + 60, x2 - 28, y2 - 18)
        draw_text_block(draw, title_box, title, BOX_TITLE_FONT, line_gap=4)
        draw_text_block(draw, body_box, body, BODY_FONT, line_gap=8)
    else:
        draw_text_block(draw, (x1 + 24, y1 + 12, x2 - 24, y2 - 12), title, BOX_TITLE_FONT, line_gap=6)


def draw_layer(
    draw: ImageDraw.ImageDraw,
    box: tuple[int, int, int, int],
    title: str,
    columns: list[tuple[str, str]],
) -> None:
    draw_panel(draw, box)
    x1, y1, x2, y2 = box
    header_bottom = y1 + 78
    draw.line((x1, header_bottom, x2, header_bottom), fill=COLORS["border"], width=3)
    draw_text_block(draw, (x1 + 20, y1 + 12, x2 - 20, header_bottom - 12), title, LAYER_TITLE_FONT, line_gap=4)

    column_width = (x2 - x1) // len(columns)
    for index, (column_title, column_body) in enumerate(columns):
        cx1 = x1 + index * column_width
        cx2 = x2 if index == len(columns) - 1 else cx1 + column_width
        if index > 0:
            draw.line((cx1, header_bottom, cx1, y2), fill=COLORS["border"], width=3)
        draw_text_block(
            draw,
            (cx1 + 18, header_bottom + 18, cx2 - 18, header_bottom + 54),
            column_title,
            LAYER_COL_TITLE_FONT,
            line_gap=4,
        )
        draw_text_block(
            draw,
            (cx1 + 18, header_bottom + 62, cx2 - 18, y2 - 16),
            column_body,
            SMALL_FONT,
            line_gap=8,
        )


def draw_app_logic_layer(draw: ImageDraw.ImageDraw, box: tuple[int, int, int, int]) -> None:
    draw_panel(draw, box)
    x1, y1, x2, y2 = box
    header_bottom = y1 + 78
    flow_top = y2 - 54
    draw.line((x1, header_bottom, x2, header_bottom), fill=COLORS["border"], width=3)
    draw.line((x1, flow_top, x2, flow_top), fill=COLORS["border"], width=3)
    draw_text_block(draw, (x1 + 20, y1 + 12, x2 - 20, header_bottom - 12), "APP / LOGIC", LAYER_TITLE_FONT, line_gap=4)

    columns = [
        ("Frontend", "NextJS UI demo\nhiển thị và gọi API"),
        ("Kiểm soát tại backend", "JWT + RBAC\nownership check\nrate limit"),
        ("Nghiệp vụ API", "Course / Enrollment /\nLesson / Admin\nAES-GCM + audit"),
    ]
    column_width = (x2 - x1) // len(columns)
    for index, (column_title, column_body) in enumerate(columns):
        cx1 = x1 + index * column_width
        cx2 = x2 if index == len(columns) - 1 else cx1 + column_width
        if index > 0:
            draw.line((cx1, header_bottom, cx1, flow_top), fill=COLORS["border"], width=3)
        draw_text_block(
            draw,
            (cx1 + 18, header_bottom + 18, cx2 - 18, header_bottom + 54),
            column_title,
            LAYER_COL_TITLE_FONT,
            line_gap=4,
        )
        draw_text_block(
            draw,
            (cx1 + 18, header_bottom + 62, cx2 - 18, flow_top - 14),
            column_body,
            SMALL_FONT,
            line_gap=8,
        )

    draw_text_block(
        draw,
        (x1 + 40, flow_top + 6, x2 - 40, y2 - 6),
        "Luồng xử lý: Frontend -> logic tại backend -> nghiệp vụ",
        SMALL_FONT,
        line_gap=2,
    )


def create_diagram() -> Path:
    image = Image.new("RGB", (WIDTH, HEIGHT), COLORS["background"])
    draw = ImageDraw.Draw(image)

    draw_text_block(
        draw,
        (220, 10, 1180, 56),
        "Kiến trúc giải pháp bảo mật",
        TITLE_FONT,
        line_gap=4,
    )

    internet_box = (430, 88, 970, 168)
    draw_ellipse_panel(draw, internet_box)
    draw_text_block(draw, internet_box, "Internet / Browser / API Client", BOX_TITLE_FONT, line_gap=4)

    net_box = (90, 240, 1310, 462)
    draw_layer(
        draw,
        net_box,
        "NET",
        [
            ("Public surface", "Chỉ public 80 và 443\nbackend và DB không lộ trực tiếp"),
            ("Nginx / HTTPS", "HTTP -> HTTPS\nTLS + reverse proxy"),
            ("API entry", "Frontend và API\nđi qua Nginx"),
        ],
    )

    app_box = (90, 548, 1310, 846)
    draw_app_logic_layer(draw, app_box)

    data_box = (90, 932, 1310, 1160)
    draw_layer(
        draw,
        data_box,
        "DATA",
        [
            ("MySQL", "users / courses /\nenrollments / lessons"),
            ("Dữ liệu bảo vệ", "password / refresh hash\nciphertext"),
            ("Audit / state", "audit log\nPENDING -> ACTIVE"),
        ],
    )

    result_box = (90, 1246, 1310, 1474)
    draw_layer(
        draw,
        result_box,
        "RESULT",
        [
            ("Truy cập đúng quyền", "Sai quyền trả 401 / 403\nbackend quyết định"),
            ("Dữ liệu được bảo vệ", "Không lưu plaintext\ntrường nhạy cảm được mã hóa"),
            ("Lesson mở sau duyệt", "PENDING chưa mở lesson\nACTIVE mới được truy cập"),
        ],
    )

    draw_arrow(draw, (700, 168), (700, 240))
    draw_arrow(draw, (700, 462), (700, 548))
    draw_arrow(draw, (700, 846), (700, 932))
    draw_arrow(draw, (700, 1160), (700, 1246))

    OUTPUT_DIR.mkdir(parents=True, exist_ok=True)
    output = OUTPUT_DIR / "so-do-kien-truc-giai-phap-bao-mat-fixed.png"
    image.save(output)
    return output


def main() -> None:
    print(create_diagram())


if __name__ == "__main__":
    main()
