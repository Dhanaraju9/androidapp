# Implementation Plan: Upgrade Subject Cards on Home Screen

## Goal
Upgrade Subject Cards on the Home Screen to be larger, display images with the same styling as Unit Cards, and refine the displayed content.

## Scope
1.  **Card Sizing**: Increase Subject Card dimensions by approximately 20%.
2.  **Image Presentation**: Align image style, quality, aspect ratio, and treatment of Subject Cards with the existing Unit 1 and Unit 2 cards.
3.  **Content Refinement**:
    *   Restrict Subject Card content to Prelims, Mains, and General categories.
    *   Remove descriptive subtitle text (subject.heroQuote).
    *   Maintain accurate dynamic counts (Units, Topics, Sets, MCQs).
4.  **Exclusions**: Skip Current Affairs Cards entirely.

## Functional Preservation
- Keep all existing click actions, navigation, data models, and feature behaviors.
- Ensure no accidental changes to MCQ Practice or Result screens.
