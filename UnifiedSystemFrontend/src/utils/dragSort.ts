import Sortable from 'sortablejs'

export interface RowDragSortOptions {
  sameParentOnly?: boolean
  onReorder: (draggedEl: HTMLElement) => void | Promise<void>
}

export function initRowDragSort(el: HTMLElement, options: RowDragSortOptions): Sortable {
  let orderBefore: string[] = []

  const snapshot = () =>
    Array.from(el.querySelectorAll<HTMLElement>('tr[data-row-key]')).map(tr => tr.dataset.rowKey ?? '')

  return Sortable.create(el, {
    handle: '.drag-handle',
    draggable: 'tr.row-draggable',
    animation: 150,
    ghostClass: 'drag-ghost',
    chosenClass: 'drag-chosen',
    onStart() {
      orderBefore = snapshot()
    },
    onMove(evt) {
      if (!options.sameParentOnly) return true
      const dragged = evt.dragged as HTMLElement
      const related = evt.related as HTMLElement | null
      if (!related) return true
      return (dragged.dataset.parentId ?? '0') === (related.dataset.parentId ?? '0')
    },
    onEnd(evt) {
      if (snapshot().join('|') === orderBefore.join('|')) return
      void options.onReorder(evt.item as HTMLElement)
    }
  })
}
