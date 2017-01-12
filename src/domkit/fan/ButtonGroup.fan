//
// Copyright (c) 2016, Brian Frank and Andy Frank
// Licensed under the Academic Free License version 3.0
//
// History:
//   17 Aug 2016  Andy Frank  Creation
//

using dom

**
** ButtonGroup groups a set of toggle or radio buttons and handles
** making sure only one button in group is selected at a time.
**
** See also: [docDomkit]`docDomkit::Controls#buttonGroup`,
** `ToggleButton`, `RadioButton`
**
@Js class ButtonGroup
{
  ** Buttons in this group.
  Elem[] buttons := [,]
  {
    set { &buttons=it; update }
  }

  ** Convenience to add a button to `buttons`.
  This add(Elem button)
  {
    this.buttons = buttons.add(button)
    return this
  }

  ** Index of selected button, or 'null' if none selected.
  Int? selected := null
  {
    set
    {
      old := &selected
      &selected = it
      update
      if (it != old) cbSelect?.call(this)
    }
  }

  ** Callback when selection in group has changed.
  Void onSelect(|This| f) { this.cbSelect = f }

  ** Mark given button as selected.
  internal Void select(Elem button)
  {
    this.selected = buttons.findIndex |b| { b === button }
  }

  ** Update group state and make sure buttons are bound to this group.
  internal Void update()
  {
    buttons.each |b,i|
    {
      if (b is ToggleButton)
      {
        t := (ToggleButton)b
        t.group = this
        t.selected = i == selected
        return
      }

      if (b is RadioButton)
      {
        r := (RadioButton)b
        r.group = this
        r.checked = i == selected
        return
      }

      throw ArgErr("Invalid button for group '$b.typeof'")
    }
  }

  private Func? cbSelect
}